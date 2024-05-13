package onlydust.com.marketplace.api.rest.api.adapter.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.OnlyDustError;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;

@ControllerAdvice
@Slf4j
public class OnlydustExceptionRestHandler {

    private static OnlyDustError onlyDustErrorFromException(final OnlyDustException exception) {
        final var httpStatus = Optional.ofNullable(HttpStatus.resolve(exception.getStatus())).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        final var errorId = UUID.randomUUID();
        if (httpStatus.is5xxServerError()) {
            LOGGER.error(format("%d error %s returned by the REST API", httpStatus.value(), errorId), exception);
        } else {
            LOGGER.warn(format("%d error %s returned by the REST API", httpStatus.value(), errorId), exception);
        }
        return new OnlyDustError()
                .id(errorId)
                .status(httpStatus.value())
                .message(httpStatus.is5xxServerError() ? httpStatus.name() : exception.getMessage());
    }

    private static String sanitizeMessage(String message) {
        return message
                .replaceAll("\\(class onlydust\\.com(\\.[a-zA-Z0-9_]+)+\\)", "")
                .replaceAll("onlydust\\.com(\\.[a-zA-Z0-9_]+)+", "");
    }

    @ExceptionHandler({OnlyDustException.class})
    protected ResponseEntity<OnlyDustError> handleOnlyDustException(final OnlyDustException exception) {
        final OnlyDustError onlyDustError = onlyDustErrorFromException(exception);
        return ResponseEntity.status(onlyDustError.getStatus()).body(onlyDustError);
    }

    @ExceptionHandler({
            BindException.class,
            JsonMappingException.class,
            HttpMessageConversionException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestPartException.class,
            ConstraintViolationException.class,
            ServletRequestBindingException.class,
    })
    protected ResponseEntity<OnlyDustError> handleBadRequests(final Exception exception) {
        return handleOnlyDustException(new OnlyDustException(
                HttpStatus.BAD_REQUEST.value(),
                sanitizeMessage(exception.getMessage()),
                exception
        ));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    protected ResponseEntity<OnlyDustError> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        final String message =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).collect(Collectors.joining(", "));

        return handleOnlyDustException(new OnlyDustException(
                HttpStatus.BAD_REQUEST.value(),
                message,
                exception
        ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<OnlyDustError> unauthorized(final AuthenticationException exception) {
        return handleOnlyDustException(new OnlyDustException(
                HttpStatus.UNAUTHORIZED.value(),
                "Missing authentication",
                exception
        ));
    }

    @ExceptionHandler({
            NoResourceFoundException.class,
            NoHandlerFoundException.class
    })
    public ResponseEntity<OnlyDustError> notFound(final NoResourceFoundException exception) {
        return handleOnlyDustException(new OnlyDustException(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                exception
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OnlyDustError> internalError(final Exception exception) {
        LOGGER.error("Internal error from unexpected runtime exception", exception);
        return handleOnlyDustException(new OnlyDustException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal error from unexpected runtime exception",
                exception
        ));
    }
}
