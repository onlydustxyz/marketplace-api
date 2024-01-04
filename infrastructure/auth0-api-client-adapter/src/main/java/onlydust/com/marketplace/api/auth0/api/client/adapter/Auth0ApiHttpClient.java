package onlydust.com.marketplace.api.auth0.api.client.adapter;

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
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.auth0.api.client.adapter.authentication.Auth0ApiAuthenticator;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class Auth0ApiHttpClient {

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Auth0ApiClientProperties properties;
  private final Auth0ApiAuthenticator auth0ApiAuthenticator;

  private HttpRequest.Builder builderFromAuthorization() throws IOException, InterruptedException {
    return HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + auth0ApiAuthenticator.getAuth0ManagementApiAccessToken());
  }

  public <RequestBody, ResponseBody> ResponseBody sendRequest(final String path,
      final HttpMethod method,
      final RequestBody requestBody,
      final Class<ResponseBody> responseClass) {
    try {
      final HttpResponse<byte[]> httpResponse = httpClient.send(
          builderFromAuthorization()
              .uri(URI.create(properties.getDomainBaseUri() + path))
              .method(method.name(),
                  isNull(requestBody) ? noBody() :
                      ofByteArray(objectMapper.writeValueAsBytes(requestBody)))
              .build(),
          HttpResponse.BodyHandlers.ofByteArray()
      );
      final int statusCode = httpResponse.statusCode();
      if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
        throw OnlyDustException.unauthorized(format("Unauthorized error when calling %s on Auth0 API", path));
      } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
        throw OnlyDustException.forbidden(format("Forbidden error when calling %s on Auth0 API", path));
      } else if (statusCode != HttpStatus.OK.value() &&
          statusCode != HttpStatus.CREATED.value() &&
          statusCode != HttpStatus.ACCEPTED.value() &&
          statusCode != HttpStatus.NO_CONTENT.value()) {
        throw OnlyDustException.internalServerError(format("Unknown error (status %d) when calling %s on " +
            "Auth0 API", statusCode, path));
      } else if (Void.class.isAssignableFrom(responseClass)) {
        return null;
      }
      return objectMapper.readValue(httpResponse.body(), responseClass);

    } catch (JsonProcessingException e) {
      throw internalServerError("Fail to serialize request", e);
    } catch (IOException | InterruptedException e) {
      throw internalServerError("Fail send request", e);
    }
  }
}
