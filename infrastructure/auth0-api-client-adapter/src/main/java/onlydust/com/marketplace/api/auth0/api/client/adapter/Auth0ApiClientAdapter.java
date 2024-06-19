package onlydust.com.marketplace.api.auth0.api.client.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.handler.codec.http.HttpMethod;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;

import java.util.concurrent.TimeUnit;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

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
        final var cachedPat = patCache.getIfPresent(githubUserId);
        if (cachedPat != null) {
            return cachedPat;
        }
        final var newPat = fetchGithubPersonalToken(githubUserId);
        patCache.put(githubUserId, newPat);
        return newPat;
    }

    @Override
    public void logout(Long githubUserId) {
        patCache.invalidate(githubUserId);
    }

    private String fetchGithubPersonalToken(Long githubUserId) {
        final var userId = encode("github|%d".formatted(githubUserId), UTF_8);
        final var response = httpClient.send("/api/v2/users/%s".formatted(userId), HttpMethod.GET, null, Auth0UserResponse.class)
                .orElseThrow(() -> OnlyDustException.internalServerError(("Could not retrieve Auth0 identities of user %s").formatted(userId)));

        return response.getIdentities().stream()
                .filter(identity -> identity.getProvider().equals("github"))
                .findFirst()
                .map(Auth0UserResponse.Identity::getAccessToken)
                .orElseThrow(() -> OnlyDustException.internalServerError(("Could not retrieve Github personal token of user %s").formatted(userId)));
    }

}
