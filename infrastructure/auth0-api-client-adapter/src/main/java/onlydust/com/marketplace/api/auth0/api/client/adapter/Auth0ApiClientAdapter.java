package onlydust.com.marketplace.api.auth0.api.client.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.user.domain.port.output.IdentityProviderPort;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Slf4j
public class Auth0ApiClientAdapter implements GithubAuthenticationPort, IdentityProviderPort {

    private final Auth0ApiHttpClient httpClient;
    private final Cache<Long, String> patCache;

    public Auth0ApiClientAdapter(Auth0ApiClientProperties properties, Auth0ApiHttpClient httpClient) {
        this.httpClient = httpClient;
        this.patCache = Caffeine.newBuilder().expireAfterWrite(properties.getPatCacheTtlInSeconds(), TimeUnit.SECONDS).build();
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
        final var userId = getAuth0UserId(githubUserId);
        final var response =
                httpClient.send("/api/v2/users/%s".formatted(userId), HttpMethod.GET, null, Auth0UserResponse.class)
                        .orElseThrow(() -> internalServerError(("Could not retrieve Auth0 identities of user %s").formatted(userId)));

        return response.identities().stream().filter(identity -> identity.provider().equals("github"))
                .findFirst().map(Auth0UserResponse.Identity::accessToken)
                .orElseThrow(() -> internalServerError(("Could not retrieve Github personal token of user %s").formatted(userId)));
    }

    private static String getAuth0UserId(Long githubUserId) {
        return encode("github|%d".formatted(githubUserId), UTF_8);
    }

    @Override
    public void deleteUser(Long githubUserId) {
        final String auth0UserId = getAuth0UserId(githubUserId);
        httpClient.send("/api/v2/users/%s".formatted(auth0UserId), HttpMethod.DELETE, null, Void.class);
    }
}
