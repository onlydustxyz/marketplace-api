package onlydust.com.marketplace.api.domain.exception;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class OnlydustExceptionTest {
    private static final Faker faker = new Faker();

    @Test
    void should_return_stack_trace_in_to_string_given_a_root_exception() {
        // Given
        final String message = faker.rickAndMorty().location();
        final int status = faker.number().randomDigit();
        final OnlydustException symeoException = OnlydustException.builder()
                .rootException(new IOException())
                .message(message)
                .status(status)
                .build();

        // When
        final String symeoExceptionToString = symeoException.toString();

        // Then
        Assertions.assertTrue(symeoExceptionToString.contains(String.format("message='%s'", message)));
        Assertions.assertTrue(symeoExceptionToString.contains(String.format("status='%s'", Integer.valueOf(status))));
        Assertions.assertTrue(symeoExceptionToString.contains(String.format("rootException=java.io.IOException\n" +
                "\tat onlydust.com.marketplace.api.domain.exception.OnlydustExceptionTest" +
                ".should_return_stack_trace_in_to_string_given_a_root_exception(OnlydustExceptionTest.java:")));
    }
}
