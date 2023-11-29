package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.GithubRepositoryLinkedToProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GithubRepositoryLinkedToProjectRepository extends JpaRepository<GithubRepositoryLinkedToProjectEntity, Long> {

    @Query(value = """
            SELECT gr.id,
                   gr.owner_login as owner,
                   gr.name,
                   gr.languages   as technologies,
                   pgr.project_id
            FROM indexer_exp.github_repos as gr
                     JOIN project_github_repos pgr ON pgr.github_repo_id = gr.id
            where gr.visibility = 'PUBLIC'""", nativeQuery = true)
    Page<GithubRepositoryLinkedToProjectEntity> findAllPublic(final Pageable pageable);
}
