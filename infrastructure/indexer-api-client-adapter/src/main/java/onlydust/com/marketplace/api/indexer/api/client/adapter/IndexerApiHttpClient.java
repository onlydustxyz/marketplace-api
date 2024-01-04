package onlydust.com.marketplace.api.indexer.api.client.adapter;

import static java.lang.String.format;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpRequest.BodyPublishers.ofByteArray;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.Data;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class IndexerApiHttpClient {

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Properties properties;

  private HttpRequest.Builder builderFromAuthorizations() {
    return HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .header("Api-Key", properties.getApiKey());
  }

  public <RequestBody, ResponseBody> ResponseBody sendRequest(final String path,
      final HttpMethod method,
      final RequestBody requestBody,
      final Class<ResponseBody> responseClass) {
    try {
      final HttpResponse<byte[]> httpResponse = httpClient.send(
          builderFromAuthorizations()
              .uri(URI.create(properties.getBaseUri() + path))
              .method(method.name(),
                  isNull(requestBody) ? noBody() :
                      ofByteArray(objectMapper.writeValueAsBytes(requestBody)))
              .build(),
          HttpResponse.BodyHandlers.ofByteArray()
      );
      final int statusCode = httpResponse.statusCode();
      final var body = httpResponse.body();
      if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
        throw OnlyDustException.unauthorized(format("Unauthorized error when calling %s on Indexer API", path));
      } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
        throw OnlyDustException.forbidden(format("Forbidden error when calling %s on Indexer API: %s", path, bodyString(body)));
      } else if (statusCode != HttpStatus.OK.value() &&
          statusCode != HttpStatus.CREATED.value() &&
          statusCode != HttpStatus.ACCEPTED.value() &&
          statusCode != HttpStatus.NO_CONTENT.value()) {
        throw OnlyDustException.internalServerError(format("Unknown error (status %d) when calling %s on " +
            "Indexer" +
            " API", statusCode, path));
      } else if (Void.class.isAssignableFrom(responseClass)) {
        return null;
      }
      return objectMapper.readValue(body, responseClass);

    } catch (JsonProcessingException e) {
      throw internalServerError("Fail to serialize request", e);
    } catch (IOException | InterruptedException e) {
      throw internalServerError("Fail send request", e);
    }
  }

  private static String bodyString(final byte[] body) {
    return body != null ? new String(body, StandardCharsets.UTF_8) : "null";
  }

  @Data
  public static class Properties {

    String baseUri;
    String apiKey;
  }
}
