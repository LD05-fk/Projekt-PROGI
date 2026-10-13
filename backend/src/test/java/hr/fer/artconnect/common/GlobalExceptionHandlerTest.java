package hr.fer.artconnect.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

// Ovdje provjeravamo kontrolere i handler. Sigurnosni filtri pripadaju AUTH zadacima.
@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({CommonWebConfiguration.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {
    @Autowired
    MockMvc mvc;

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    void apiGreskaVracaStatusIKod(ErrorCode code) throws Exception {
        var result = provjeriGresku(mvc.perform(get("/test/api/{code}", code)),
                code.getHttpStatus().value(), code.name());
        if (code == ErrorCode.INTERNAL_ERROR) {
            result.andExpect(jsonPath("$.error.details").doesNotExist());
        } else {
            result.andExpect(jsonPath("$.error.message").value("Provjerite podatke."))
                    .andExpect(jsonPath("$.error.details[0].field").value("title"))
                    .andExpect(jsonPath("$.error.details[0].issue").value("Naslov nije ispravan."));
        }
    }

    @ParameterizedTest
    @CsvSource({"constraints,400,VALIDATION_ERROR", "upload,413,FILE_TOO_LARGE",
            "denied,403,FORBIDDEN", "unexpected,500,INTERNAL_ERROR", "notfound,404,NOT_FOUND"})
    void handlerObradujeRazliciteIznimke(String kind, int status, String code) throws Exception {
        var result = provjeriGresku(mvc.perform(get("/test/error/{kind}", kind)), status, code);
        if (kind.equals("constraints")) {
            result.andExpect(jsonPath("$.error.details[0].field").value("title"));
        } else {
            result.andExpect(jsonPath("$.error.details").doesNotExist());
        }
        if (status == 500) {
            assertThat(result.andReturn().getResponse().getContentAsString())
                    .doesNotContain("interni podatak", "stackTrace", "IllegalStateException");
        }
    }

    @Test
    void prazanNaslovImaHrvatskuPoruku() throws Exception {
        provjeriGresku(mvc.perform(post("/test/body").header("Accept-Language", "en")
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}")), 400, "VALIDATION_ERROR")
                .andExpect(jsonPath("$.error.details[0].field").value("title"))
                .andExpect(jsonPath("$.error.details[0].issue").value("Polje ne smije biti prazno."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "", "{\"title\":{}}"})
    void neispravanJsonVraca400(String body) throws Exception {
        provjeriGresku(mvc.perform(post("/test/body").contentType(MediaType.APPLICATION_JSON)
                .content(body)), 400, "VALIDATION_ERROR")
                .andExpect(jsonPath("$.error.details[0].field").value("body"));
    }

    @ParameterizedTest
    @CsvSource({"/test/uuid/krivi-id,id", "/test/required,title", "/test/method?page=0,page"})
    void neispravanParametarVracaImePolja(String url, String field) throws Exception {
        provjeriGresku(mvc.perform(get(url)), 400, "VALIDATION_ERROR")
                .andExpect(jsonPath("$.error.details[0].field").value(field));
    }

    @Test
    void nedostajeDatoteka() throws Exception {
        provjeriGresku(mvc.perform(multipart("/test/file")), 400, "VALIDATION_ERROR")
                .andExpect(jsonPath("$.error.details[0].field").value("file"));
    }

    @Test
    void neispravanOdgovorKontroleraJeGreskaPosluzitelja() throws Exception {
        provjeriGresku(mvc.perform(get("/test/bad-return")), 500, "INTERNAL_ERROR")
                .andExpect(jsonPath("$.error.details").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=0", "page=abc", "page=", "pageSize=0", "pageSize=101",
            "pageSize=abc", "pageSize=", "sort=owner.password", "sort="})
    void neispravnoStranicenjeISortiranjeVraca400(String query) throws Exception {
        provjeriGresku(mvc.perform(get("/test/pages?" + query)), 400, "VALIDATION_ERROR")
                .andExpect(jsonPath("$.error.details[0].field").value(query.split("=")[0]));
    }

    @Test
    void popisImaPodatkeIMetapodatke() throws Exception {
        mvc.perform(get("/test/pages")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("djelo"))
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.pageSize").value(20))
                .andExpect(jsonPath("$.meta.total").value(125));
    }

    @Test
    void datumJeIsoTekstICuvaPomak() throws Exception {
        mvc.perform(post("/test/date").contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-11-20T23:59:00+01:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-11-20T23:59:00+01:00"));
    }

    private ResultActions provjeriGresku(ResultActions result, int status, String code) throws Exception {
        return result.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value(code))
                .andExpect(jsonPath("$.error.message").isString())
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {
        private final Validator validator;

        TestController(Validator validator) {
            this.validator = validator;
        }

        @GetMapping("/api/{code}")
        void api(@PathVariable ErrorCode code) {
            throw new ApiException(code, "Provjerite podatke.",
                    List.of(new FieldIssue("title", "Naslov nije ispravan.")));
        }

        @GetMapping("/error/{kind}")
        void error(@PathVariable String kind) {
            throw switch (kind) {
                case "constraints" -> new ConstraintViolationException(validator.validate(new Input("")));
                case "upload" -> new MaxUploadSizeExceededException(50 * 1024 * 1024);
                case "denied" -> new AccessDeniedException("interni podatak");
                case "notfound" -> new ApiException(ErrorCode.NOT_FOUND, "Prilika nije pronađena.");
                default -> new IllegalStateException("interni podatak");
            };
        }

        @PostMapping("/body")
        Input body(@Valid @RequestBody Input input) {
            return input;
        }

        @GetMapping("/method")
        int method(@RequestParam @Min(1) int page) {
            return page;
        }

        @GetMapping("/bad-return")
        @NotNull
        String badReturn() {
            return null;
        }

        @GetMapping("/uuid/{id}")
        UUID uuid(@PathVariable UUID id) {
            return id;
        }

        @GetMapping("/required")
        String required(@RequestParam String title) {
            return title;
        }

        @PostMapping("/file")
        void file(@RequestPart MultipartFile file) {
        }

        @GetMapping("/pages")
        PageResponse<String> pages(@Valid @ModelAttribute PageRequestParams params,
                @RequestParam(required = false) String sort) {
            return PageResponse.from(new PageImpl<>(List.of("djelo"),
                    params.toPageable(SortParser.parse(sort, Set.of("deadline"))), 125));
        }

        @PostMapping("/date")
        DateInput date(@RequestBody DateInput input) {
            return input;
        }
    }

    record Input(@NotBlank String title) {}
    record DateInput(OffsetDateTime date) {}
}
