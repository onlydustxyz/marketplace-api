package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CustomIgnoredContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CustomIgnoredContributionsRepository extends JpaRepository<CustomIgnoredContributionEntity,
        CustomIgnoredContributionEntity.Id> {

    @Query("select c from CustomIgnoredContributionEntity c where c.id.projectId = ?1")
    List<CustomIgnoredContributionEntity> findAllByProjectId(UUID projectId);

    @Modifying
    @Query("update CustomIgnoredContributionEntity c set c.ignored = false where c.id.projectId = ?1 and c.id" +
           ".contributionId not in ?2")
    void unignoreOtherContributions(UUID projectId, List<String> ignoredContributionIds);

}
