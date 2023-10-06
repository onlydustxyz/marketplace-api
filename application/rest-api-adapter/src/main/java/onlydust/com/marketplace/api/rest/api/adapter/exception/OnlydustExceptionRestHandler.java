package onlydust.com.marketplace.api.rest.api.adapter.exception;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.OnlyDustError;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@ControllerAdvice
@Slf4j
public class OnlydustExceptionRestHandler {

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<OnlyDustError> handleException(final Exception exception) {
        final OnlyDustError onlyDustError = onlyDustErrorFromException(exception);
        return ResponseEntity.status(onlyDustError.getStatus()).body(onlyDustError);
    }

    private static OnlyDustError onlyDustErrorFromException(final Exception exception) {
        final OnlydustException onlydustException = getOnlydustExceptionFromException(exception);
        final HttpStatus httpStatus =
                Optional.ofNullable(
                                HttpStatus.resolve(onlydustException.getStatus()))
                        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        final UUID errorId = UUID.randomUUID();
        final OnlyDustError onlyDustError = new OnlyDustError();
        onlyDustError.setStatus(httpStatus.value());
        onlyDustError.setMessage(httpStatus.name());
        onlyDustError.setId(errorId);
        LOGGER.error(String.format("Error %s returned from the REST API with stacktrace :", errorId),
                isNull(onlydustException.getRootException()) ?
                        onlydustException : onlydustException.getRootException());
        return onlyDustError;
    }

    private static OnlydustException getOnlydustExceptionFromException(Exception exception) {
        OnlydustException onlydustException;
        if (exception instanceof OnlydustException) {
            onlydustException = (OnlydustException) exception;
        } else {
            onlydustException =
                    OnlydustException.builder()
                            .rootException(exception)
                            .message("Internal error from unexpected runtime exception")
                            .status(500)
                            .build();
        }
        return onlydustException;
    }
}
