package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IgnoredContributionsRepository extends JpaRepository<IgnoredContributionEntity,
        IgnoredContributionEntity.Id> {
}
