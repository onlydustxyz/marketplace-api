package onlydust.com.marketplace.api.postgres.adapter.repository;


import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;

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
            REFRESH MATERIALIZED VIEW CONCURRENTLY repo_languages;
            REFRESH MATERIALIZED VIEW CONCURRENTLY project_languages;
            REFRESH MATERIALIZED VIEW CONCURRENTLY m_programs_projects;
            REFRESH MATERIALIZED VIEW CONCURRENTLY m_active_programs_projects;
            REFRESH MATERIALIZED VIEW CONCURRENTLY bi.project_contribution_stats;
            REFRESH MATERIALIZED VIEW CONCURRENTLY bi.project_reward_stats;
            REFRESH MATERIALIZED VIEW CONCURRENTLY bi.m_project_similarities;
            """)
    void refreshStats();

    @Query(nativeQuery = true, value = """
            select pgr.project_id
            from indexer_exp.grouped_contributions gc
            join project_github_repos pgr on pgr.github_repo_id = gc.repo_id
            where gc.contribution_uuid = :contributionUuid
            """)
    List<UUID> findProjectIdsLinkedToContributionUuid(UUID contributionUuid);

    @Query(nativeQuery = true, value = """
            select distinct pgr.github_repo_id
            from project_github_repos pgr
            where pgr.github_repo_id in :repoIds and (coalesce(:projectId) is null or pgr.project_id != :projectId)
            """)
    List<Long> findReposLinkedToAnotherProject(List<Long> repoIds, UUID projectId);
}
