package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.DepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DepositRepository extends JpaRepository<DepositEntity, UUID> {
}
