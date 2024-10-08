package onlydust.com.marketplace.api.auth0.api.client.adapter.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.auth0.api.client.adapter.Auth0ApiClientProperties;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import static java.net.http.HttpRequest.BodyPublishers.ofByteArray;

@AllArgsConstructor
public class Auth0ApiAuthenticator {
    private static final String CACHE_KEY = "management-api-access-token";
    public static final int CACHE_TTL_LEEWAY_IN_SECONDS = 60;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Auth0ApiClientProperties properties;
    private final Cache<String, Auth0ManagementApiAccessTokenResponse> accessTokenCache = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, Auth0ManagementApiAccessTokenResponse>() {
                @Override
                public long expireAfterCreate(String key, Auth0ManagementApiAccessTokenResponse value, long currentTime) {
                    return TimeUnit.SECONDS.toNanos(value.getExpiresIn() - CACHE_TTL_LEEWAY_IN_SECONDS);
                }

                @Override
                public long expireAfterUpdate(String key, Auth0ManagementApiAccessTokenResponse value, long currentTime, @NonNegative long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(String key, Auth0ManagementApiAccessTokenResponse value, long currentTime, @NonNegative long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    public String getAuth0ManagementApiAccessToken() {
        return accessTokenCache.get(CACHE_KEY, s -> negotiateAuth0ManagementApiAccessToken()).getAccessToken();
    }

    private Auth0ManagementApiAccessTokenResponse negotiateAuth0ManagementApiAccessToken() {
        try {
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

            return objectMapper.readValue(httpResponse.body(), Auth0ManagementApiAccessTokenResponse.class);
        } catch (Exception e) {
            throw OnlyDustException.internalServerError("Failed to negotiate auth0 api access token", e);
        }
    }
}
