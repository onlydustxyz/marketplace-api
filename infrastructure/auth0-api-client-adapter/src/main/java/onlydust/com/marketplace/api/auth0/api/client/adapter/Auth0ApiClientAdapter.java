package onlydust.com.marketplace.api.auth0.api.client.adapter;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import onlydust.com.marketplace.api.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.http.HttpMethod;

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


  private String fetchGithubPersonalToken(Long githubUserId) {
    final var userId = encode("github|%d".formatted(githubUserId), UTF_8);
    final var response = httpClient.sendRequest("/api/v2/users/%s".formatted(userId),
        HttpMethod.GET, null, Auth0UserResponse.class);

    return response.getIdentities().stream()
        .filter(identity -> identity.getProvider().equals("github"))
        .findFirst()
        .map(Auth0UserResponse.Identity::getAccessToken)
        .orElseThrow(() -> OnlyDustException.internalServerError(("Could not retrieve Github personal token " +
            "of user %s").formatted(userId)));
  }

}
