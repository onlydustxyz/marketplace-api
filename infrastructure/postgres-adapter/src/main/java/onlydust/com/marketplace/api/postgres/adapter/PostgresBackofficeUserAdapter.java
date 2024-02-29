package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.write.BackofficeUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BackofficeUserRepository;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import onlydust.com.marketplace.user.domain.port.output.BackofficeUserStoragePort;

import java.util.Optional;

@AllArgsConstructor
public class PostgresBackofficeUserAdapter implements BackofficeUserStoragePort {
    private final BackofficeUserRepository backofficeUserRepository;

    @Override
    public Optional<BackofficeUser> getUserByEmail(@NonNull String email) {
        return backofficeUserRepository.findByEmail(email).map(BackofficeUserEntity::toDomain);
    }

    @Override
    public void save(@NonNull BackofficeUser user) {
        backofficeUserRepository.save(BackofficeUserEntity.fromDomain(user));
    }
}
