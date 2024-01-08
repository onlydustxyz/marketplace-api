package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.GithubRepositoryLinkedToProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.GithubRepositoryLinkedToProjectEntity.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface GithubRepositoryLinkedToProjectRepository extends JpaRepository<GithubRepositoryLinkedToProjectEntity, Id> {

    @Query(value = """
            SELECT gr.id,
                   gr.owner_login AS owner,
                   gr.name,
                   rl.languages   AS technologies,
                   pgr.project_id
            FROM indexer_exp.github_repos AS gr
             JOIN project_github_repos pgr ON pgr.github_repo_id = gr.id
             JOIN (
                SELECT repo_id, jsonb_object_agg(language, line_count) AS languages
                FROM indexer_exp.github_repo_languages
                GROUP BY repo_id
             ) rl ON rl.repo_id = gr.id
            WHERE gr.visibility = 'PUBLIC'
            AND (COALESCE(:projectIds) IS NULL OR pgr.project_id IN (:projectIds))
            """, nativeQuery = true)
    Page<GithubRepositoryLinkedToProjectEntity> findAllPublicForProjectsIds(final Pageable pageable,
                                                                            final List<UUID> projectIds);


}
