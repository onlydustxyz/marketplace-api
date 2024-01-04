package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubRepoViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GithubRepoViewEntityRepository extends JpaRepository<GithubRepoViewEntity, Long> {

  @Query(value = """
      SELECT
          r.id,
          owner.login as owner,
          r.name,
          r.html_url,
          r.updated_at,
          r.description,
          r.stars_count,
          r.forks_count
      FROM
           indexer_exp.github_repos r
      INNER JOIN indexer_exp.github_accounts owner ON r.owner_id = owner.id
      WHERE
          EXISTS(
              SELECT 1 
              FROM indexer_exp.contributions c 
              INNER JOIN indexer_exp.github_repos gr on gr.id = c.repo_id
              INNER JOIN project_github_repos pgr ON pgr.github_repo_id = r.id
              WHERE 
                  c.repo_id = r.id AND contributor_id = :contributorId 
                  AND gr.visibility = 'PUBLIC' AND
                  (COALESCE(:projectIds) IS NULL OR pgr.project_id IN (:projectIds))
          ) 
          AND (COALESCE(:repoIds) IS NULL OR r.id IN (:repoIds))
      ORDER BY 
          r.name 
      """, nativeQuery = true)
  List<GithubRepoViewEntity> listReposByContributor(Long contributorId,
      List<UUID> projectIds,
      List<Long> repoIds);

  @Override
  @Query(value = """
      SELECT
          r.id,
          owner.login as owner,
          r.name,
          r.html_url,
          r.updated_at,
          r.description,
          r.stars_count,
          r.forks_count
      FROM
           indexer_exp.github_repos r
      INNER JOIN indexer_exp.github_accounts owner ON r.owner_id = owner.id
      WHERE
          r.id = :repoId and r.visibility = 'PUBLIC'
      """, nativeQuery = true)
  @NonNull
  Optional<GithubRepoViewEntity> findById(@NonNull Long repoId);
}
