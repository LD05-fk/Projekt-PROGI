package hr.fer.artconnect.common;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> data, PageMeta meta) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(),
                new PageMeta(page.getNumber() + 1, page.getSize(), page.getTotalElements()));
    }

    public record PageMeta(int page, int pageSize, long total) {
    }
}
