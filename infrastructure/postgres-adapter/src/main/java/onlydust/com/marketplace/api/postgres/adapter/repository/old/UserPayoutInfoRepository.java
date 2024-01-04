package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import java.util.Optional;
import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserPayoutInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPayoutInfoRepository extends JpaRepository<UserPayoutInfoEntity, UUID> {

  Optional<UserPayoutInfoEntity> findByUserId(final UUID userId);
}
