package hr.fer.artconnect.common;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class PaginationTest {
    @Test
    void zadaneVrijednosti() {
        var page = new PageRequestParams().toPageable(Sort.unsorted());
        assertThat(page.getPageNumber()).isZero();
        assertThat(page.getPageSize()).isEqualTo(20);
    }

    @ParameterizedTest
    @CsvSource({"1,1", "2,20", "3,100"})
    void dopusteneGranice(int page, int size) {
        var params = new PageRequestParams();
        params.setPage(page);
        params.setPageSize(size);
        var result = params.toPageable(Sort.unsorted());
        assertThat(result.getPageNumber()).isEqualTo(page - 1);
        assertThat(result.getPageSize()).isEqualTo(size);
    }

    @ParameterizedTest
    @CsvSource({"0,20", "-1,20", "1,0", "1,-1", "1,101"})
    void neispravneGranice(int page, int size) {
        var params = new PageRequestParams();
        params.setPage(page);
        params.setPageSize(size);
        assertThatThrownBy(() -> params.toPageable(Sort.unsorted()))
                .isInstanceOfSatisfying(ApiException.class,
                        error -> assertThat(error.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void odgovorCuvaStranicuVelicinuIUkupanBroj() {
        var response = PageResponse.from(new PageImpl<>(List.of("djelo"), PageRequest.of(1, 20), 125));
        assertThat(response.data()).containsExactly("djelo");
        assertThat(response.meta()).isEqualTo(new PageResponse.PageMeta(2, 20, 125));
        var empty = PageResponse.from(new PageImpl<String>(List.of(), PageRequest.of(8, 20), 125));
        assertThat(empty.data()).isEmpty();
        assertThat(empty.meta()).isEqualTo(new PageResponse.PageMeta(9, 20, 125));
    }

    @Test
    void smjerSortiranja() {
        assertThat(SortParser.parse(null, Set.of("deadline")).isUnsorted()).isTrue();
        assertThat(SortParser.parse("deadline", Set.of("deadline")).getOrderFor("deadline").isAscending()).isTrue();
        assertThat(SortParser.parse("-deadline", Set.of("deadline")).getOrderFor("deadline").isDescending()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "-", "--deadline", "+deadline", "deadline,desc", "deadline ", "owner.password"})
    void nedopustenoSortiranje(String sort) {
        assertThatThrownBy(() -> SortParser.parse(sort, Set.of("deadline")))
                .isInstanceOfSatisfying(ApiException.class, error -> {
                    assertThat(error.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(error.getDetails().getFirst().field()).isEqualTo("sort");
                });
    }
}
