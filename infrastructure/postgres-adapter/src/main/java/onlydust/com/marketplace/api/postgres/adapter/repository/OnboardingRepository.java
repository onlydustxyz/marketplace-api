package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OnboardingRepository extends JpaRepository<OnboardingEntity, UUID> {
}
