package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import onlydust.com.marketplace.user.domain.port.input.BackofficeUserFacadePort;
import onlydust.com.marketplace.user.domain.port.output.BackofficeUserStoragePort;

import java.util.Set;

@AllArgsConstructor
public class BackofficeUserService implements BackofficeUserFacadePort {

    private final BackofficeUserStoragePort backofficeUserStoragePort;

    @Override
    public BackofficeUser getUserByIdentity(@NonNull final BackofficeUser.Identity identity) {
        return backofficeUserStoragePort.getUserByEmail(identity.email()).orElseGet(() -> {
            final var user = new BackofficeUser(
                    UserId.random(),
                    identity.email(),
                    identity.name(),
                    Set.of(BackofficeUser.Role.BO_READER),
                    identity.avatarUrl());
            backofficeUserStoragePort.save(user);
            return user;
        });
    }
}
