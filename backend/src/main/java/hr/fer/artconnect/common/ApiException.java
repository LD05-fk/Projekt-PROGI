package hr.fer.artconnect.common;

import java.util.List;

// Poruka se prikazuje korisniku, pa mora biti na hrvatskom.
public class ApiException extends RuntimeException {
    private final ErrorCode code;
    private final List<FieldIssue> details;

    public ApiException(ErrorCode code, String messageHr, List<FieldIssue> details) {
        super(messageHr);
        this.code = code;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public ApiException(ErrorCode code, String messageHr) {
        this(code, messageHr, List.of());
    }

    public ErrorCode getCode() {
        return code;
    }

    public List<FieldIssue> getDetails() {
        return details;
    }
}
