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
                   gr.languages   AS technologies,
                   pgr.project_id
            FROM indexer_exp.github_repos AS gr
                     JOIN project_github_repos pgr ON pgr.github_repo_id = gr.id
            WHERE gr.visibility = 'PUBLIC'
            """, nativeQuery = true)
    Page<GithubRepositoryLinkedToProjectEntity> findAllPublic(final Pageable pageable);

    @Query(value = """
            SELECT gr.id,
                   gr.owner_login AS owner,
                   gr.name,
                   gr.languages   AS technologies,
                   pgr.project_id
            FROM indexer_exp.github_repos AS gr
                     JOIN project_github_repos pgr ON pgr.github_repo_id = gr.id
            WHERE gr.visibility = 'PUBLIC'
            AND pgr.project_id IN (:projectIds)
            """, nativeQuery = true)
    Page<GithubRepositoryLinkedToProjectEntity> findAllPublicForProjectsIds(final Pageable pageable, final List<UUID> projectIds);


}
