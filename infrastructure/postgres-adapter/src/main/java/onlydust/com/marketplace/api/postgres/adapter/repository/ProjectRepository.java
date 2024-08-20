package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID>, JpaSpecificationExecutor<ProjectEntity> {

    Page<ProjectEntity> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query(nativeQuery = true, value = """
            select distinct project_id
            from project_leads
            where user_id = :userId
            """)
    List<UUID> getProjectLedIdsForUser(UUID userId);

    @Query(nativeQuery = true, value = """
            select distinct pgr.project_id
            from iam.users u
            join indexer_exp.contributions c on c.contributor_id = u.github_user_id
            join project_github_repos pgr on pgr.github_repo_id = c.repo_id
            where u.id = :userId
            """)
    List<UUID> getProjectContributedOnIdsForUser(UUID userId);

    @Modifying
    @Query(nativeQuery = true, value = """
            REFRESH MATERIALIZED VIEW CONCURRENTLY top_project_recommendations;
            """)
    void refreshRecommendations();
}
