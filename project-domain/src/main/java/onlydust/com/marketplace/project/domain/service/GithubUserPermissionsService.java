package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.port.input.GithubUserPermissionsFacadePort;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationInfoPort;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;

@AllArgsConstructor
public class GithubUserPermissionsService implements GithubUserPermissionsFacadePort {
    private final GithubAuthenticationPort githubAuthenticationPort;
    private final GithubAuthenticationInfoPort githubAuthenticationInfoPort;

    @Override
    public void logout(Long githubUserId) {
        githubAuthenticationPort.logout(githubUserId)
                .ifPresent(githubAuthenticationInfoPort::logout);
    }

    @Override
    public boolean isUserAuthorizedToApplyOnProject(Long githubUserId) {
        final var accessToken = githubAuthenticationPort.getGithubPersonalToken(githubUserId);
        final var authorizedScopes = githubAuthenticationInfoPort.getAuthorizedScopes(accessToken);
        return authorizedScopes.contains("public_repo") || authorizedScopes.contains("repo");
    }
}
