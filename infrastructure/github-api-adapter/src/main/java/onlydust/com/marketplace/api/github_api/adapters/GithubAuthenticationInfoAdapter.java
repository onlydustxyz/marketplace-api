package onlydust.com.marketplace.api.github_api.adapters;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationInfoPort;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Slf4j
public class GithubAuthenticationInfoAdapter implements GithubAuthenticationInfoPort {

    private final GithubHttpClient httpClient;
    private final Map<String, Set<String>> scopesByToken = new ConcurrentHashMap<>();

    @Override
    public void logout(String accessToken) {
        scopesByToken.remove(accessToken);
    }

    @Override
    public Set<String> getAuthorizedScopes(String accessToken) {
        return scopesByToken.computeIfAbsent(accessToken, this::fetchAuthorizedScopes);
    }

    private Set<String> fetchAuthorizedScopes(String accessToken) {
        try {
            final var response = httpClient.fetch(httpClient.buildURI(""), accessToken);
            return response.headers()
                    .firstValue("x-oauth-scopes")
                    .map(scopes -> Set.of(scopes.split(", ")))
                    .orElse(Set.of());
        } catch (Exception e) {
            LOGGER.error("Failed to get user's authorized scopes from Github API", e);
            return Set.of();
        }
    }
}
