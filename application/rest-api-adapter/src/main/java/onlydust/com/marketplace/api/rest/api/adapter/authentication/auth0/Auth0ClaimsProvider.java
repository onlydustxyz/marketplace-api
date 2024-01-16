package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.ClaimsProvider;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@AllArgsConstructor
public class Auth0ClaimsProvider implements ClaimsProvider<Auth0JwtClaims> {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Auth0Properties properties;

    @Override
    public Auth0JwtClaims getClaimsFromAccessToken(@NonNull String accessToken) {
        try {
            final var request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.jwksUrl + "userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200)
                throw OnlyDustException.internalServerError("Unable to get user info from Auth0: [%d] %s".formatted(response.statusCode(), response.body()));

            return objectMapper.readValue(response.body(), Auth0JwtClaims.class);
        } catch (Exception e) {
            throw OnlyDustException.internalServerError("Unable to get user claims from Auth0", e);
        }
    }
}
