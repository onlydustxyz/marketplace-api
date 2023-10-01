package onlydust.com.marketplace.api.domain.exception;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OnlydustExceptionTest {
    private static final Faker faker = new Faker();

    @Test
    void should_return_stack_trace_in_to_string_given_a_root_exception() {
        // Given
        final String code = faker.rickAndMorty().character();
        final String message = faker.rickAndMorty().location();
        final OnlydustException symeoException = OnlydustException.builder()
                .rootException(new IOException())
                .code(code)
                .message(message)
                .build();

        // When
        final String symeoExceptionToString = symeoException.toString();

        // Then
        Assertions.assertTrue(symeoExceptionToString.contains(String.format("code='%s'", code)));
        Assertions.assertTrue(symeoExceptionToString.contains(String.format("message='%s'", message)));
        Assertions.assertTrue(symeoExceptionToString.contains(String.format("rootException=java.io.IOException\n" +
                "\tat onlydust.com.marketplace.api.domain.exception.OnlydustExceptionTest" +
                ".should_return_stack_trace_in_to_string_given_a_root_exception(OnlydustExceptionTest.java:")));
    }

    @Test
    void should_return_exception_type_given_starting_with_F_and_not_functional_for_exception_code_starting_with_T() {
        // Given
        final OnlydustException functionalOnlydustException =
                OnlydustException.builder()
                        .code("F.CODE_TEST")
                        .message("Test message for function Symeo Exception")
                        .build();
        final OnlydustException technicalOnlydustException =
                OnlydustException.builder()
                        .code("T.CODE_TEST")
                        .message("Test message for function Symeo Exception")
                        .build();

        // Then
        assertTrue(functionalOnlydustException.isFunctional());
        assertFalse(functionalOnlydustException.isTechnical());
        assertTrue(technicalOnlydustException.isTechnical());
        assertFalse(technicalOnlydustException.isFunctional());
    }
}
