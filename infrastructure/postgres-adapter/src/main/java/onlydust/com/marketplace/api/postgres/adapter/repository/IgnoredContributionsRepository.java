package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IgnoredContributionsRepository extends JpaRepository<IgnoredContributionEntity,
        IgnoredContributionEntity.Id> {

    @Query("select c from IgnoredContributionEntity c where c.id.projectId = ?1")
    List<IgnoredContributionEntity> findAllByProjectId(UUID projectId);


    @Modifying
    @Query("delete from IgnoredContributionEntity c where c.id.projectId = ?1 and c.id.contributionId not in ?2")
    void deleteOtherContributions(UUID projectId, List<String> ignoredContributionIds);
}
