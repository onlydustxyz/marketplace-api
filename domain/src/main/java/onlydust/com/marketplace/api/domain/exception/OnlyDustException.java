package onlydust.com.marketplace.api.domain.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static java.util.Objects.isNull;

@SuppressWarnings("unused")
@Getter
@Slf4j
public class OnlyDustException extends RuntimeException {

    int status;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public OnlyDustException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public OnlyDustException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public static OnlyDustException internalServerError(final String message) {
        return new OnlyDustException(500, message);
    }

    public static OnlyDustException internalServerError(final String message, final Throwable cause) {
        return new OnlyDustException(500, message, cause);
    }

    public static OnlyDustException invalidInput(final String message) {
        return new OnlyDustException(400, message);
    }

    public static OnlyDustException invalidInput(final String message, final Throwable cause) {
        return new OnlyDustException(400, message, cause);
    }

    public static OnlyDustException notFound(final String message) {
        return new OnlyDustException(404, message);
    }

    public static OnlyDustException notFound(final String message, final Throwable cause) {
        return new OnlyDustException(404, message, cause);
    }

    public static OnlyDustException unauthorized(final String message) {
        return new OnlyDustException(401, message);
    }

    public static OnlyDustException unauthorized(final String message, final Throwable cause) {
        return new OnlyDustException(401, message, cause);
    }

    @Override
    public String toString() {
        return String.format("OnlyDustException{message='%s', status=%d, rootException=%s}",
                this.getMessage(),
                this.status,
                getRootExceptionAsString());
    }

    private String getRootExceptionAsString() {
        if (isNull(this.getCause())) {
            return "null";
        }

        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                this.getCause().printStackTrace(pw);
                return sw.toString();
            }
        } catch (IOException e) {
            final String errorMessage = "Failed to convert rootException stack trace to string";
            LOGGER.error("Failed to convert rootException stack trace to string", e);
            return errorMessage;
        }
    }
}
