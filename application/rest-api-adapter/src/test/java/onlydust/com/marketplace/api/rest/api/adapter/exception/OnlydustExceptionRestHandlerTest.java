package onlydust.com.marketplace.api.rest.api.adapter.exception;

import onlydust.com.marketplace.api.contract.model.OnlyDustError;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OnlydustExceptionRestHandlerTest {

    private final OnlydustExceptionRestHandler onlydustExceptionRestHandler = new OnlydustExceptionRestHandler();

    private void assertExceptionIsMappedToError(Exception exception, int expectedStatus, String expectedCode) {
        // When
        final ResponseEntity<OnlyDustError> onlyDustErrorResponseEntity =
                onlydustExceptionRestHandler.handleException(exception);

        // Then
        assertEquals(expectedStatus, onlyDustErrorResponseEntity.getStatusCodeValue());
        assertEquals(expectedStatus, onlyDustErrorResponseEntity.getBody().getStatus());
        assertEquals(expectedCode, onlyDustErrorResponseEntity.getBody().getMessage());
        assertNotNull(onlyDustErrorResponseEntity.getBody().getId());
    }

    @Test
    void should_map_exception_not_instance_of_onlydust_exception() {
        // Given
        final Exception exception = new NullPointerException();

        final int expectedStatus = 500;
        final String expectedCode = HttpStatus.INTERNAL_SERVER_ERROR.name();
        assertExceptionIsMappedToError(exception, expectedStatus, expectedCode);
    }

    @Test
    void should_get_404_exception() {
        // Given
        final OnlydustException projectNotFound = OnlydustException.builder()
                .status(404)
                .message("Project not found")
                .build();

        final int expectedStatus = 404;
        final String expectedCode = HttpStatus.NOT_FOUND.name();
        assertExceptionIsMappedToError(projectNotFound, expectedStatus, expectedCode);
    }
}
