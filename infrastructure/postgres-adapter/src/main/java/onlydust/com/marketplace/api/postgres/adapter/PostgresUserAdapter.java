package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStoragePort {

    private final CustomUserRepository customUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfile getProfileById(UUID userId) {
        return customUserRepository.findProfileById(userId);
    }
}
