package onlydust.com.marketplace.api.domain.exception;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class OnlydustException extends RuntimeException {

    @NonNull
    Integer status;
    @NonNull
    String message;
    Exception rootException;

    @Override
    public String toString() {
        final String rootExceptionAsString = Objects.isNull(this.rootException) ?
                "null" : getRootExceptionAsString();
        return "OnlydustException{" +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
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
