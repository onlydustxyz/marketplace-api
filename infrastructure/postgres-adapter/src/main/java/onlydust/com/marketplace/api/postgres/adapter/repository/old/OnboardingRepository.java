package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingRepository extends JpaRepository<OnboardingEntity, UUID> {

}
