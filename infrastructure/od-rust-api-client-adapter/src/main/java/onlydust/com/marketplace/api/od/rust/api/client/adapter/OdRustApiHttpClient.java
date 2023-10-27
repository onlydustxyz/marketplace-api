package onlydust.com.marketplace.api.od.rust.api.client.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraAuthentication;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.String.format;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpRequest.BodyPublishers.ofByteArray;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.domain.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.IMPERSONATION_HEADER;

@AllArgsConstructor
public class OdRustApiHttpClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Properties properties;

    private HttpRequest.Builder builderFromAuthorizations(final HasuraAuthentication authentication) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Api-Key", properties.getApiKey())
                .header("Authorization", BEARER_PREFIX + authentication.getJwt());

        if (authentication.getImpersonationHeader() != null) {
            builder = builder.header(IMPERSONATION_HEADER, authentication.getImpersonationHeader());
        }
        return builder;
    }

    public <RequestBody, ResponseBody> ResponseBody sendRequest(final String path,
                                                                final HttpMethod method,
                                                                final RequestBody requestBody,
                                                                final Class<ResponseBody> responseClass,
                                                                final HasuraAuthentication authentication) {
        try {
            final HttpResponse<byte[]> httpResponse = httpClient.send(
                    builderFromAuthorizations(authentication)
                            .uri(URI.create(properties.getBaseUri() + path))
                            .method(method.name(),
                                    isNull(requestBody) ? noBody() :
                                            ofByteArray(objectMapper.writeValueAsBytes(requestBody)))
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray()
            );
            final int statusCode = httpResponse.statusCode();
            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                throw OnlyDustException.unauthorized(format("Unauthorized error when calling %s on Rust API", path));
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                throw OnlyDustException.forbidden(format("Forbidden error when calling %s on Rust API", path));
            } else if (statusCode != HttpStatus.OK.value() &&
                       statusCode != HttpStatus.CREATED.value() &&
                       statusCode != HttpStatus.ACCEPTED.value() &&
                       statusCode != HttpStatus.NO_CONTENT.value()) {
                throw OnlyDustException.internalServerError(format("Unknown error (status %d) when calling %s on Rust" +
                                                                   " API", statusCode, path));
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

    @Data
    public static class Properties {
        String baseUri;
        String apiKey;
    }
}
