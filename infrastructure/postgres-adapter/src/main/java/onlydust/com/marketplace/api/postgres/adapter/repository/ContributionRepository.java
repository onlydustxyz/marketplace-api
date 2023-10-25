package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubRepoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortProjectViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ContributionRepository extends JpaRepository<ContributionViewEntity, String> {

    @Query(value = """
            SELECT 
                c.id,
                c.created_at,
                c.completed_at,
                c.type,
                c.status,
                COALESCE(pr.number, i.number, cr_pr.number) as github_number,
                COALESCE(pr.title, i.title, cr_pr.title) as github_title,
                COALESCE(pr.html_url, i.html_url, cr_pr.html_url) as github_html_url,
                COALESCE(pr.body, i.body, cr_pr.body) as github_body,
                p.name as project_name,
                r.name as repo_name
            FROM 
                indexer_exp.contributions c
            LEFT JOIN indexer_exp.github_pull_requests pr ON pr.id = pull_request_id
            LEFT JOIN indexer_exp.github_issues i ON i.id = issue_id
            LEFT JOIN indexer_exp.github_code_reviews cr on cr.id = c.code_review_id
            LEFT JOIN indexer_exp.github_pull_requests cr_pr on cr_pr.id = cr.pull_request_id
            INNER JOIN indexer_exp.github_repos r on r.id = c.repo_id
            INNER JOIN public.project_github_repos pgr on pgr.github_repo_id = r.id
            INNER JOIN public.project_details p on p.project_id = pgr.project_id        
            WHERE 
                contributor_id = :contributorId AND
                (:projectIds IS NULL OR p.project_id IN :projectIds) AND
                (:repoIds IS NULL OR r.id IN :repoIds) AND
                (:types IS NULL OR c.type IN :types) AND
                (:statuses IS NULL OR c.status IN :statuses)
            """, nativeQuery = true)
    Page<ContributionViewEntity> findContributionsForContributor(Long contributorId,
                                                                 List<UUID> projectIds,
                                                                 List<Long> repoIds,
                                                                 List<ContributionViewEntity.Type> types,
                                                                 List<ContributionViewEntity.Status> statuses,
                                                                 Pageable pageable);

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
            INNER JOIN project_github_repos pgr ON pgr.github_repo_id = r.id
            WHERE
                EXISTS(
                    SELECT 1 
                    FROM indexer_exp.contributions c 
                    WHERE 
                        c.repo_id = r.id AND contributor_id = :contributorId AND 
                        (:projectIds IS NULL OR pgr.project_id IN :projectIds) AND
                        (:repoIds IS NULL OR r.id IN :repoIds)
                ) 
            """, nativeQuery = true)
    List<GithubRepoViewEntity> listReposByContributor(Long contributorId,
                                                      List<UUID> projectIds,
                                                      List<Long> repoIds);

    @Query(value = """
            SELECT
                p.project_id as id,
                p.key,
                p.name,
                p.short_description,
                p.long_description,
                p.logo_url,
                p.telegram_link,
                p.hiring,
                p.visibility
            FROM 
                 project_details p
            WHERE
                EXISTS(
                SELECT 1 
                FROM 
                    project_github_repos pgr
                INNER JOIN indexer_exp.contributions c on c.repo_id = pgr.github_repo_id 
                WHERE 
                    p.project_id = pgr.project_id AND
                    contributor_id = :contributorId AND 
                    (:projectIds IS NULL OR p.project_id IN :projectIds) AND
                    (:repoIds IS NULL OR pgr.github_repo_id IN :repoIds)
                ) 
            """, nativeQuery = true)
    List<ShortProjectViewEntity> listProjectsByContributor(Long contributorId,
                                                           List<UUID> projectIds,
                                                           List<Long> repoIds);
}
