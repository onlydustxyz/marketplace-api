package onlydust.com.marketplace.api.rest.api.adapter.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.OnlyDustError;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;

@ControllerAdvice
@Slf4j
public class OnlydustExceptionRestHandler {

    private static OnlyDustError onlyDustErrorFromException(final OnlyDustException exception) {
        final HttpStatus httpStatus = Optional.ofNullable(HttpStatus.resolve(exception.getStatus()))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        final UUID errorId = UUID.randomUUID();
        final OnlyDustError onlyDustError = new OnlyDustError();
        onlyDustError.setId(errorId);
        onlyDustError.setStatus(httpStatus.value());
        if (httpStatus.is5xxServerError()) {
            onlyDustError.setMessage(httpStatus.name());
        } else {
            onlyDustError.setMessage(exception.getMessage());
        }
        if (httpStatus.is5xxServerError()) {
            LOGGER.error(format("%d error %s returned by the REST API", httpStatus.value(), errorId), exception);
        } else {
            LOGGER.warn(format("%d error %s returned by the REST API", httpStatus.value(), errorId), exception);
        }
        return onlyDustError;
    }

    @ExceptionHandler({OnlyDustException.class})
    protected ResponseEntity<OnlyDustError> handleOnlyDustException(final OnlyDustException exception) {
        final OnlyDustError onlyDustError = onlyDustErrorFromException(exception);
        return ResponseEntity.status(onlyDustError.getStatus()).body(onlyDustError);
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

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    protected ResponseEntity<OnlyDustError> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception) {
        return handleOnlyDustException(new OnlyDustException(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                exception
        ));
    }

    @ExceptionHandler({BindException.class})
    protected ResponseEntity<OnlyDustError> handleBindException(final BindException exception) {
        return handleOnlyDustException(new OnlyDustException(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                exception
        ));
    }

    @ExceptionHandler({JsonMappingException.class})
    protected ResponseEntity<OnlyDustError> handleJsonMappingException(final JsonMappingException exception) {
        return handleOnlyDustException(new OnlyDustException(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                exception
        ));
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public ResponseEntity<OnlyDustError> unauthorized(AuthenticationException exception) {
        return handleOnlyDustException(new OnlyDustException(
                HttpStatus.UNAUTHORIZED.value(),
                "Missing authentication",
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
