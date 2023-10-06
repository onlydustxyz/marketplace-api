package onlydust.com.marketplace.api.rest.api.adapter.exception;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.OnlyDustError;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
        OnlydustException onlydustException;
        if (exception instanceof OnlydustException) {
            onlydustException = (OnlydustException) exception;
        } else {
            onlydustException =
                    OnlydustException.builder()
                            .rootException(exception)
                            .code(RestApiExceptionCode.INTERNAL_SERVER_ERROR)
                            .message("Internal server error")
                            .build();
        }

        int status;
        if (onlydustException.getCode().equals(RestApiExceptionCode.UNAUTHORIZED)) {
            status = 403;
        } else if (onlydustException.isTechnical()) {
            status = 500;
        } else {
            status = 400;
        }

        final OnlyDustError onlyDustError = new OnlyDustError();
        onlyDustError.setStatus(status);
        onlyDustError.setTitle(onlydustException.getMessage());
        onlyDustError.setType(onlydustException.getCode());
        LOGGER.error("Error returned from the REST API", isNull(onlydustException.getRootException()) ?
                onlydustException : onlydustException.getRootException());
        return onlyDustError;
    }
}
