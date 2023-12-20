package onlydust.com.marketplace.api.auth0.api.client.adapter.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.auth0.api.client.adapter.Auth0ApiClientProperties;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.BodyPublishers.ofByteArray;

@AllArgsConstructor
public class Auth0ApiAuthenticator {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Auth0ApiClientProperties properties;

    public String getAuth0ManagementApiAccessToken() throws IOException, InterruptedException {
        final HttpResponse<byte[]> httpResponse = httpClient.send(HttpRequest.newBuilder()
                .uri(URI.create(properties.getDomainBaseUri() + "/oauth/token"))
                .header("Content-Type", "application/json")
                .POST(ofByteArray(objectMapper.writeValueAsBytes(Auth0ManagementApiAccessTokenRequest.builder()
                        .grantType("client_credentials")
                        .clientId(properties.getClientId())
                        .clientSecret(properties.getClientSecret())
                        .audience(properties.getDomainBaseUri() + "/api/v2/")
                        .build())))
                .build(), HttpResponse.BodyHandlers.ofByteArray()
        );

        final int statusCode = httpResponse.statusCode();
        if (statusCode != HttpStatus.OK.value()) {
            throw OnlyDustException.internalServerError(("Error (status %d) when negotiating a new " +
                                                         "management access token for Auth0 API: %s").formatted(statusCode, httpResponse.body()));
        }

        final var auth0ManagementApiAccessTokenResponse = objectMapper.readValue(httpResponse.body(),
                Auth0ManagementApiAccessTokenResponse.class);
        return auth0ManagementApiAccessTokenResponse.getAccessToken();
    }
}
