package onlydust.com.marketplace.kernel.exception;

import com.github.javafaker.Faker;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OnlyDustExceptionTest {

  private static final Faker faker = new Faker();

  @Test
  void should_return_stack_trace_in_to_string_given_a_root_exception() {
    // Given
    final String message = faker.rickAndMorty().location();
    final int status = faker.number().randomDigit();
    final OnlyDustException onlyDustException = new OnlyDustException(status, message, new IOException("raclette"));

    // When
    final String exceptionString = onlyDustException.toString();

    // Then
    System.out.println(exceptionString);
    Assertions.assertTrue(exceptionString.contains(String.format("message='%s'", message)));
    Assertions.assertTrue(exceptionString.contains(String.format("status=%d", status)));
    Assertions.assertTrue(exceptionString.contains("""
        java.io.IOException: raclette
        	at onlydust.com.marketplace.kernel.exception.OnlyDustExceptionTest.should_return_stack_trace_in_to_string_given_a_root_exception(OnlyDustExceptionTest.java:17)
        """));
  }
}
