package onlydust.com.marketplace.api.webhook;

import static java.lang.String.format;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@Slf4j
@AllArgsConstructor
public class WebhookHttpClient {

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final Config config;

  public <RequestBody> void post(final RequestBody requestBody) {
    try {
      final HttpResponse<byte[]> httpResponse =
          httpClient.send(HttpRequest.newBuilder().uri(config.getUrl())
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(requestBody)))
              .build(), HttpResponse.BodyHandlers.ofByteArray());

      final int statusCode = httpResponse.statusCode();
      if (statusCode >= 400) {
        throw OnlyDustException.internalServerError(format("Error (status %d) when calling webhook %s",
            statusCode, config.getUrl()));
      }
    } catch (JsonProcessingException e) {
      throw internalServerError("Fail to serialize request", e);
    } catch (IOException | InterruptedException e) {
      throw internalServerError("Fail send request to webhook %s".formatted(config.getUrl()), e);
    }
  }


}
