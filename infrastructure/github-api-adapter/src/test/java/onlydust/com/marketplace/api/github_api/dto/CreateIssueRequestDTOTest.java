package onlydust.com.marketplace.api.github_api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateIssueRequestDTOTest {


  @Test
  void should_serialize() throws JsonProcessingException {
    // Given
    final ObjectMapper objectMapper = new ObjectMapper();
    final CloseIssueRequestDTO closeIssueRequestDTO = CloseIssueRequestDTO.builder().build();

    // When
    final byte[] bytes = objectMapper.writeValueAsBytes(closeIssueRequestDTO);

    // Then
    Assertions.assertNotNull(bytes);
    Assertions.assertTrue(bytes.length > 0);
  }
}
