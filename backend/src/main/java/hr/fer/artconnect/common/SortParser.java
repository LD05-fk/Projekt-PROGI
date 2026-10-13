package hr.fer.artconnect.common;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;

public class SortParser {
    public static Sort parse(String sort, Set<String> allowedFields) {
        if (sort == null) {
            return Sort.unsorted();
        }
        boolean descending = sort.startsWith("-");
        String field = descending ? sort.substring(1) : sort;
        if (field.isBlank() || !allowedFields.contains(field)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Neki podaci nisu ispravni.",
                    List.of(new FieldIssue("sort", "Polje ili oblik sortiranja nije dopušten.")));
        }
        return Sort.by(descending ? Sort.Direction.DESC : Sort.Direction.ASC, field);
    }
}
