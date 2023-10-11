package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.UserProfileView;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class UserService implements UserFacadePort {

    private final UserStoragePort userStoragePort;

    @Override
    public User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity) {
        return userStoragePort
                .getUserByGithubId(githubUserIdentity.getGithubUserId())
                .orElseGet(() -> {
                    final var user = User.builder()
                            .id(UUID.randomUUID())
                            // TODO .permissions()
                            .permissions(List.of("me"))
                            .githubUserId(githubUserIdentity.getGithubUserId())
                            .avatarUrl(githubUserIdentity.getGithubAvatarUrl())
                            .login(githubUserIdentity.getGithubLogin())
                            .build();
                    userStoragePort.createUser(user);
                    return user;
                });
    }

    @Override
    public UserProfileView getProfileById(UUID userId) {
        return userStoragePort.getProfileById(userId);
    }

}
