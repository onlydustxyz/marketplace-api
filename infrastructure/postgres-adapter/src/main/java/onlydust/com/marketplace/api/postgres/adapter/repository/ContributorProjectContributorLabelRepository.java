package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ContributorProjectContributorLabelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ContributorProjectContributorLabelRepository extends JpaRepository<ContributorProjectContributorLabelEntity,
        ContributorProjectContributorLabelEntity.PrimaryKey> {

    @Query("""
            DELETE FROM ContributorProjectContributorLabelEntity c
            WHERE c.projectContributorLabel.projectId = :projectId
              AND c.githubUserId = :contributorId
            """)
    @Modifying
    void deleteByProjectIdAndContributorId(UUID projectId, Long contributorId);
}
