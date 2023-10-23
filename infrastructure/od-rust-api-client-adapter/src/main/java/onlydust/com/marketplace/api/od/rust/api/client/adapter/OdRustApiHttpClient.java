package onlydust.com.marketplace.api.od.rust.api.client.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static onlydust.com.marketplace.api.domain.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.IMPERSONATION_HEADER;

@AllArgsConstructor
public class OdRustApiHttpClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Properties properties;

    public <Request, Response> Response sendRequest(final Request request, final Class<Response> responseClass,
                                                    final String jwt,
                                                    final Optional<String> optionalHasuraImpersonationHeader) {
        try {
            final HttpResponse<byte[]> httpResponse = httpClient.send(builderFromAuthorizations(jwt,
                            optionalHasuraImpersonationHeader).uri(URI.create("http://api.onlydust.xyz/api/payments"))
                            .POST(
                                    HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(request))).build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();
            if (statusCode == 403) {
                throw OnlyDustException.unauthorized("Unauthorized to request a reward");
            } else if (statusCode == 401) {
                throw OnlyDustException.forbidden("Forbidden to request a reward");
            } else {
                if (statusCode != 200) {
                    throw OnlyDustException.internalServerError(String.format("Unknown http status %s", statusCode));
                }
                return objectMapper.readValue(httpResponse.body(), responseClass);
            }
        } catch (JsonProcessingException e) {
            throw internalServerError("Fail to serialize reward request", e);
        } catch (IOException | InterruptedException e) {
            throw internalServerError("Fail send reward request", e);
        }
    }

    private static HttpRequest.Builder builderFromAuthorizations(final String jwt,
                                                                 final Optional<String> optionalHasuraImpersonationHeader) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().headers("Authorization", BEARER_PREFIX + jwt);

        if (optionalHasuraImpersonationHeader.isPresent()) {
            builder = builder.headers(IMPERSONATION_HEADER, optionalHasuraImpersonationHeader.get());
        }
        return builder;
    }

    @Data
    public static class Properties {
        String baseUrl;
    }
}
