package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.PayoutPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutPreferenceRepository extends JpaRepository<PayoutPreferenceEntity, PayoutPreferenceEntity.PrimaryKey> {
}
