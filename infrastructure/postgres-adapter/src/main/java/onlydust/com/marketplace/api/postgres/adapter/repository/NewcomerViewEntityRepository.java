package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.NewcomerViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NewcomerViewEntityRepository extends JpaRepository<NewcomerViewEntity, Long> {

  @Query(value = """
      SELECT c.contributor_id                    AS id,
             c.contributor_login                 AS login,
             c.contributor_html_url              AS html_url,
             c.contributor_avatar_url            AS avatar_url,
             u.id IS NOT NULL                    AS is_registered,
             upi.cover                           AS cover,
             COALESCE(upi.location, ga.location) AS location,
             COALESCE(upi.bio, ga.bio)           AS bio,
             MIN(c.created_at)                   AS first_contribution_created_at
      FROM indexer_exp.contributions c
               JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id
               LEFT JOIN iam.users u ON u.github_user_id = c.contributor_id
               LEFT JOIN user_profile_info upi ON upi.id = u.id
               LEFT JOIN indexer_exp.github_accounts ga ON ga.id = c.contributor_id
      WHERE pgr.project_id = :projectId
      GROUP BY c.contributor_id,
               c.contributor_login,
               c.contributor_html_url,
               c.contributor_avatar_url,
               u.id,
               upi.cover,
               upi.location,
               ga.location,
               upi.bio,
               ga.bio
      """, nativeQuery = true)
  Page<NewcomerViewEntity> findAllByProjectId(UUID projectId, Pageable pageable);
}
