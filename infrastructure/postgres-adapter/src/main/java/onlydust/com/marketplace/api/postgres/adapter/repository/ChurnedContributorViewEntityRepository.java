package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ChurnedContributorViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChurnedContributorViewEntityRepository extends JpaRepository<ChurnedContributorViewEntity, Long> {

  @Query(value = """
          WITH latest_contributions_per_user AS
                   (SELECT DISTINCT ON (contributor_id) id,
                                                        contributor_id,
                                                        contributor_login,
                                                        contributor_html_url,
                                                        contributor_avatar_url,
                                                        repo_id,
                                                        completed_at
                    FROM indexer_exp.contributions
                    ORDER BY contributor_id, completed_at DESC NULLS LAST)
          SELECT c.contributor_id         AS id,
                 c.contributor_login      AS login,
                 c.contributor_html_url   AS html_url,
                 c.contributor_avatar_url AS avatar_url,
                 u.id IS NOT NULL         AS is_registered,
                 upi.cover                AS cover,
                 c.id                     AS last_contribution_id,
                 c.completed_at           AS last_contribution_completed_at,
                 gr.id                    AS last_contributed_repo_id,
                 gr.owner_login           AS last_contributed_repo_owner,
                 gr.name                  AS last_contributed_repo_name,
                 gr.html_url              AS last_contributed_repo_html_url,
                 gr.description           AS last_contributed_repo_description
          FROM latest_contributions_per_user c
          JOIN indexer_exp.github_repos gr ON gr.id = c.repo_id
          JOIN project_github_repos pgr ON pgr.github_repo_id = gr.id
          LEFT JOIN iam.users u ON u.github_user_id = c.contributor_id
          LEFT JOIN user_profile_info upi ON upi.id = u.id
          WHERE
              completed_at < current_date - :threshold AND
              pgr.project_id = :projectId
      """,
      countQuery = """
          SELECT COUNT(DISTINCT contributor_id)
          FROM indexer_exp.contributions c
          JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id
          WHERE
              completed_at < current_date - :threshold AND
              pgr.project_id = :projectId
          """, nativeQuery = true)
  Page<ChurnedContributorViewEntity> findAllByProjectId(UUID projectId, Integer threshold, Pageable pageable);
}
