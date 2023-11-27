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
    @Query(value = """
            delete from custom_ignored_contributions cic
            where cic.project_id = ?1 and cic.contribution_id not in (
                select c.id
                from indexer_exp.contributions c
                join indexer_exp.github_repos gr on gr.id = c.repo_id and gr.visibility = 'PUBLIC'
                join project_github_repos pgr on pgr.project_id = ?1 and pgr.github_repo_id = gr.id
            )
            """, nativeQuery = true)
    void deleteContributionsThatAreNotPartOfTheProjectAnymore(UUID projectId);
}
