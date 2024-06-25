package onlydust.com.marketplace.api.auth0.api.client.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.handler.codec.http.HttpMethod;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class Auth0ApiClientAdapter implements GithubAuthenticationPort {

    private final Auth0ApiHttpClient httpClient;
    private final Cache<Long, String> patCache;

    public Auth0ApiClientAdapter(Auth0ApiClientProperties properties, Auth0ApiHttpClient httpClient) {
        this.httpClient = httpClient;
        this.patCache = Caffeine.newBuilder()
                .expireAfterWrite(properties.getPatCacheTtlInSeconds(), TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getGithubPersonalToken(Long githubUserId) {
        return patCache.get(githubUserId, this::fetchGithubPersonalToken);
    }

    @Override
    public Optional<String> logout(Long githubUserId) {
        return Optional.ofNullable(patCache.asMap().remove(githubUserId));
    }

    private String fetchGithubPersonalToken(Long githubUserId) {
        final var userId = encode("github|%d".formatted(githubUserId), UTF_8);
        final var response = httpClient.send("/api/v2/users/%s".formatted(userId), HttpMethod.GET, null, Auth0UserResponse.class)
                .orElseThrow(() -> internalServerError(("Could not retrieve Auth0 identities of user %s").formatted(userId)));

        return response.identities().stream()
                .filter(identity -> identity.provider().equals("github"))
                .findFirst()
                .map(Auth0UserResponse.Identity::accessToken)
                .orElseThrow(() -> internalServerError(("Could not retrieve Github personal token of user %s").formatted(userId)));
    }

}
