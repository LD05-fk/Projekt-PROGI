package hr.fer.artconnect.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record ErrorResponse(ErrorBody error) {
    public ErrorResponse(ErrorCode code, String message, List<FieldIssue> details) {
        this(new ErrorBody(code, message, details));
    }

    public record ErrorBody(ErrorCode code, String message,
            @JsonInclude(JsonInclude.Include.NON_EMPTY) List<FieldIssue> details) {
    }
}
