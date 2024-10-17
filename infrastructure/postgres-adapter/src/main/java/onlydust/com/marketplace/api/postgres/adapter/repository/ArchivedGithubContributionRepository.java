package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ArchivedGithubContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivedGithubContributionRepository extends JpaRepository<ArchivedGithubContributionEntity, Long> {
}
