package onlydust.com.marketplace.api.auth0.api.client.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.port.output.GithubAuthenticationPort;
import org.springframework.http.HttpMethod;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@AllArgsConstructor
public class Auth0ApiClientAdapter implements GithubAuthenticationPort {

    private final Auth0ApiHttpClient httpClient;

    @Override
    public String getGithubPersonalToken(Long githubUserId) {
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
