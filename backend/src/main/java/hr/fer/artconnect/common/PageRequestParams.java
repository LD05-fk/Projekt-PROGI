package hr.fer.artconnect.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// Kontroler prima ovaj objekt kao @Valid @ModelAttribute.
public class PageRequestParams {
    @Min(value = 1, message = "{validation.page.min}")
    private int page = 1;

    @Min(value = 1, message = "{validation.pageSize.min}")
    @Max(value = 100, message = "{validation.pageSize.max}")
    private int pageSize = 20;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Pageable toPageable(Sort sort) {
        // Ova provjera vrijedi i kad se klasa koristi izvan kontrolera.
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            String field = page < 1 ? "page" : "pageSize";
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Neki podaci nisu ispravni.",
                    List.of(new FieldIssue(field, "Stranica mora biti najmanje 1, a veličina između 1 i 100.")));
        }
        // API broji stranice od 1, a Spring od 0.
        return PageRequest.of(page - 1, pageSize, sort);
    }
}
