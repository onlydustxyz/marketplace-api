package onlydust.com.marketplace.api.auth0.api.client.adapter.deprecated;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.auth0.api.client.adapter.Auth0ApiClientAdapter;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;

import java.util.Objects;

@AllArgsConstructor
@Deprecated
public class RetroCompatibleGithubAuthenticationAdapter implements GithubAuthenticationPort {

    private final AuthenticationService authenticationService;
    private final Auth0ApiClientAdapter auth0ApiClientAdapter;

    @Override
    public String getGithubPersonalToken(Long githubUserId) {
        final var hasuraAuthentication = authenticationService.tryGetHasuraAuthentication();
        if (hasuraAuthentication.isPresent()) {
            if (!Objects.equals(hasuraAuthentication.get().getUser().getGithubUserId(), githubUserId)) {
                throw OnlyDustException.internalServerError(
                        "Invalid use of RetroCompatibleGithubAuthenticationAdapter. " +
                        "It is expected to be called with the authenticated user's " +
                        "githubUserId (%d), but it was called with %d".formatted(
                                hasuraAuthentication.get().getUser().getGithubUserId(),
                                githubUserId));
            }
            return hasuraAuthentication.get().getClaims().getGithubAccessToken();
        }
        return auth0ApiClientAdapter.getGithubPersonalToken(githubUserId);
    }
}
