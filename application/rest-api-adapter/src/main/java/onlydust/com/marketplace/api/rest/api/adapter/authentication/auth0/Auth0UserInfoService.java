package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.checkerframework.checker.index.qual.NonNegative;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class Auth0UserInfoService {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Auth0Properties properties;
    private final JWTVerifier jwtVerifier;

    private final Cache<String, Auth0JwtClaims> userInfoCache = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, Auth0JwtClaims>() {
                @Override
                public long expireAfterCreate(String accessToken, Auth0JwtClaims value, long currentTime) {
                    final long expirationTime = getExpirationTime(accessToken);
                    return TimeUnit.SECONDS.toNanos(expirationTime) - TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
                }

                @Override
                public long expireAfterUpdate(String accessToken, Auth0JwtClaims value, long currentTime, @NonNegative long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(String accessToken, Auth0JwtClaims value, long currentTime, @NonNegative long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    public Auth0JwtClaims getUserInfo(String accessToken) throws IOException {
        return userInfoCache.get(accessToken, this::fetchUserInfo);
    }

    private Auth0JwtClaims fetchUserInfo(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.userInfoUrl))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200)
                throw OnlyDustException.internalServerError("Unable to get user info from Auth0: [%d] %s".formatted(response.statusCode(), response.body()));

            return objectMapper.readValue(response.body(), Auth0JwtClaims.class);
        } catch (Exception e) {
            throw OnlyDustException.internalServerError("Failed to fetch user info from auth0", e);
        }
    }

    private long getExpirationTime(String accessToken) {
        final DecodedJWT decodedJwt = jwtVerifier.verify(accessToken);
        final Auth0JwtAccessToken payload;
        try {
            payload = objectMapper.readValue(
                    new String(Base64.getUrlDecoder().decode(decodedJwt.getPayload()), StandardCharsets.UTF_8),
                    Auth0JwtAccessToken.class);
        } catch (JsonProcessingException e) {
            throw OnlyDustException.unauthorized("Could not decode access-token payload", e);
        }
        return payload.getExpiresAt();
    }

}
