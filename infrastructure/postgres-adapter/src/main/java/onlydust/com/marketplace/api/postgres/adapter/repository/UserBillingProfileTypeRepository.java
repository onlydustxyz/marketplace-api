package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserBillingProfileTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserBillingProfileTypeRepository extends JpaRepository<UserBillingProfileTypeEntity, UUID> {
}
