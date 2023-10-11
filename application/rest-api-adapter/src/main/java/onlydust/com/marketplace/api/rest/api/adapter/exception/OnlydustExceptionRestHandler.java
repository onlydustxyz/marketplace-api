package onlydust.com.marketplace.api.rest.api.adapter.exception;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.OnlyDustError;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@ControllerAdvice
@Slf4j
public class OnlydustExceptionRestHandler {

    private static OnlyDustError onlyDustErrorFromException(final OnlydustException exception) {
        final HttpStatus httpStatus = Optional.ofNullable(HttpStatus.resolve(exception.getStatus()))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        final UUID errorId = UUID.randomUUID();
        final OnlyDustError onlyDustError = new OnlyDustError();
        onlyDustError.setStatus(httpStatus.value());
        onlyDustError.setMessage(httpStatus.name());
        onlyDustError.setId(errorId);
        if (httpStatus.is5xxServerError()) {
            LOGGER.error(String.format("Error %s returned from the REST API with stacktrace :", errorId),
                    isNull(exception.getCause()) ? exception : exception.getCause());
        } else {
            LOGGER.warn(String.format("Error %s returned from the REST API with stacktrace :", errorId),
                    isNull(exception.getCause()) ? exception : exception.getCause());
        }
        return onlyDustError;
    }

    @ExceptionHandler({OnlydustException.class})
    protected ResponseEntity<OnlyDustError> handleOnlyDustException(final OnlydustException exception) {
        final OnlyDustError onlyDustError = onlyDustErrorFromException(exception);
        return ResponseEntity.status(onlyDustError.getStatus()).body(onlyDustError);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public ResponseEntity<OnlyDustError> unauthorized(AuthenticationException exception) {
        return handleOnlyDustException(new OnlydustException(
                HttpStatus.UNAUTHORIZED.value(),
                "Missing authentication",
                exception
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OnlyDustError> internalError(final Exception exception) {
        return handleOnlyDustException(new OnlydustException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal error from unexpected runtime exception",
                exception
        ));
    }
}
