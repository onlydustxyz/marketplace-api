package onlydust.com.marketplace.api.domain.exception;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

@Data
@Builder
@Slf4j
public class OnlydustException extends Exception {

    @NonNull
    String code;
    @NonNull
    String message;
    Exception rootException;


    public static OnlydustException getOnlydustException(final String message, final String code,
                                                         final Exception rootException) {
        return OnlydustException.builder()
                .message(message)
                .code(code)
                .rootException(rootException)
                .build();
    }

    public static OnlydustException getOnlydustException(final String message, final String code) {
        return OnlydustException.builder()
                .message(message)
                .code(code)
                .build();
    }

    public Boolean isFunctional() {
        return this.getCode().startsWith("F.");
    }

    public Boolean isTechnical() {
        return this.getCode().startsWith("T.");
    }

    @Override
    public String toString() {
        final String rootExceptionAsString = Objects.isNull(this.rootException) ?
                "null" : getRootExceptionAsString();
        return "OnlydustException{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", rootException=" + rootExceptionAsString +
                '}';
    }

    private String getRootExceptionAsString() {
        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                this.rootException.printStackTrace(pw);
                return sw.toString();
            }
        } catch (IOException e) {
            final String errorMessage = "Failed to convert rootException stack trace to string";
            LOGGER.error("Failed to convert rootException stack trace to string", e);
            return errorMessage;
        }
    }
}
