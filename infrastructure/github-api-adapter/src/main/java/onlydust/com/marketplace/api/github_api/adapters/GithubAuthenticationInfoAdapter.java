package onlydust.com.marketplace.api.github_api.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationInfoPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
public class GithubAuthenticationInfoAdapter implements GithubAuthenticationInfoPort {

    private final GithubHttpClient httpClient;
    private final Map<String, Set<String>> scopesByToken = new HashMap<>();

    @Override
    public Set<String> getAuthorizedScopes(String accessToken) {
        return scopesByToken.computeIfAbsent(accessToken, this::fetchAuthorizedScopes);
    }

    private Set<String> fetchAuthorizedScopes(String accessToken) {
        final var response = httpClient.fetch(httpClient.buildURI(""), accessToken);
        return response.headers()
                .firstValue("x-oauth-scopes")
                .map(scopes -> Set.of(scopes.split(", ")))
                .orElseThrow(() -> internalServerError("Failed to fetch authorized scopes"));
    }
}