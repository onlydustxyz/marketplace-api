package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.user.domain.port.input.AppUserFacadePort;
import onlydust.com.marketplace.user.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.user.domain.port.output.AppUserStoragePort;
import onlydust.com.marketplace.user.domain.port.output.GithubOAuthAppPort;
import onlydust.com.marketplace.user.domain.port.output.GithubUserStoragePort;
import onlydust.com.marketplace.user.domain.port.output.IdentityProviderPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class AppUserService implements AppUserFacadePort {

    private final AppUserStoragePort appUserStoragePort;
    private final GithubOAuthAppPort githubOAuthAppPort;
    private final IdentityProviderPort identityProviderPort;
    private final GithubUserStoragePort githubUserStoragePort;
    private final IndexerPort indexerPort;
    private final UserObserverPort userObserverPort;


    @Override
    @Transactional
    public AuthenticatedUser getUserByGithubIdentity(GithubUserIdentity githubUserIdentity, boolean readOnly) {
        return appUserStoragePort
                .getRegisteredUserByGithubId(githubUserIdentity.githubUserId())
                .map(user -> {
                    if (!readOnly)
                        appUserStoragePort.updateUserLastSeenAt(user.id(), new Date());

                    return user;
                })
                .orElseGet(() -> {
                    if (readOnly) {
                        throw notFound("User %d not found".formatted(githubUserIdentity.githubUserId()));
                    }

                    final var user = appUserStoragePort.createUser(AuthenticatedUser.builder()
                            .id(UUID.randomUUID())
                            .roles(List.of(AuthenticatedUser.Role.USER))
                            .githubUserId(githubUserIdentity.githubUserId())
                            .avatarUrl(githubUserIdentity.avatarUrl())
                            .login(githubUserIdentity.login())
                            .email(githubUserIdentity.email())
                            .build());

                    userObserverPort.onUserSignedUp(user);
                    return user;
                });
    }

    @Override
    @Transactional
    public void resetAndReplaceUser(UUID appUserId, String newGithubLogin, String githubOAuthAppId, String githubOAuthAppSecret) {
        final Long currentGithubUserId = appUserStoragePort.getGithubUserId(appUserId)
                .orElseThrow(() -> OnlyDustException.internalServerError("User %s github id not found".formatted(appUserId)));
        final List<GithubUserIdentity> githubUserIdentities = githubUserStoragePort.searchUsers(newGithubLogin);
        if (githubUserIdentities.isEmpty()) {
            throw OnlyDustException.internalServerError("Github user %s not found".formatted(newGithubLogin));
        }
        final GithubUserIdentity newGithubUserIdentity = githubUserIdentities.get(0);
        indexerPort.indexUser(newGithubUserIdentity.githubUserId());
        String githubPersonalToken = identityProviderPort.getGithubPersonalToken(currentGithubUserId);
        githubOAuthAppPort.deleteGithubOAuthApp(githubOAuthAppId, githubOAuthAppSecret, githubPersonalToken);
        identityProviderPort.deleteUser(currentGithubUserId);
        appUserStoragePort.replaceUser(appUserId, currentGithubUserId, newGithubUserIdentity.githubUserId(), newGithubUserIdentity.login(),
                newGithubUserIdentity.avatarUrl());
    }
}
