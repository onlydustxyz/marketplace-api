package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionDetailsQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ContributionDetailsViewEntityRepository extends JpaRepository<ContributionDetailsQueryEntity, String> {

    @Query(value = """
                SELECT 
                   c.id,
                   c.created_at,
                   c.completed_at,
                   c.type,
                   c.status,
                   c.github_number,
                   c.github_status,
                   c.github_title,
                   c.github_html_url,
                   c.github_body,
                   c.github_comments_count,
                   c.github_author_id,
                   c.github_author_login,
                   c.github_author_html_url,
                   user_avatar_url(c.github_author_id, c.github_author_avatar_url) as github_author_avatar_url,
                   p.id as project_id,
                   p.name as project_name,
                   p.slug as project_key,
                   p.short_description as project_short_description,
                   p.logo_url as project_logo_url,
                   p.visibility as project_visibility,
                   c.repo_id,
                   c.repo_owner_login as repo_owner,
                   c.repo_name,
                   c.repo_html_url,
                   COALESCE(closing_issues.links,closing_pull_requests.links, reviewed_pull_requests.links) as links,
                   c.contributor_login,
                   user_avatar_url(c.contributor_id, c.contributor_avatar_url) as contributor_avatar_url,
                   c.contributor_id,
                   c.contributor_html_url,
                   u.id IS NOT NULL AS contributor_is_registered,
                   c.pr_review_state,
                   case
                       when c.type = 'PULL_REQUEST' then (select gpr.commit_count
                                                          from indexer_exp.github_pull_requests gpr
                                                          where gpr.id = c.pull_request_id)
                       end                                                                                   as github_commits_count,
                   case
                       when c.type = 'PULL_REQUEST' then (select gprcc.commit_count
                                                          from indexer_exp.github_pull_request_commit_counts gprcc
                                                          where gprcc.pull_request_id = c.pull_request_id and gprcc.author_id = c.contributor_id)
                       end                                                                                   as github_user_commits_count
                FROM
                   indexer_exp.contributions c
                INNER JOIN indexer_exp.github_repos gr on c.repo_id = gr.id and gr.visibility = 'PUBLIC'
                INNER JOIN public.project_github_repos pgr on pgr.github_repo_id = gr.id
                INNER JOIN public.projects p on p.id = pgr.project_id        
                LEFT JOIN iam.users u on u.github_user_id = c.contributor_id
                LEFT JOIN LATERAL (
                    SELECT 
                        jsonb_agg(jsonb_build_object(
                            'type', 'ISSUE',
                            'github_number', i.number,
                            'github_status', i.status,
                            'github_title', i.title,
                            'github_html_url', i.html_url,
                            'github_body', i.body,
                            'github_author_id', i.author_id,
                            'github_author_login', i.author_login,
                            'github_author_html_url', i.author_html_url,
                            'github_author_avatar_url', user_avatar_url(i.author_id, i.author_avatar_url),
                            'is_mine', c.contributor_id = i.author_id,
                            'repo_id', i.repo_id,
                            'repo_owner', i.repo_owner_login,
                            'repo_name', i.repo_name,
                            'repo_html_url', i.repo_html_url
                        )
                    ) as links
                    FROM
                        indexer_exp.github_pull_requests_closing_issues pr_ci
                    INNER JOIN indexer_exp.github_issues i ON i.id = pr_ci.issue_id
                    WHERE 
                        pr_ci.pull_request_id = c.pull_request_id
                    GROUP BY 
                        c.id
                ) AS closing_issues ON TRUE
                LEFT JOIN LATERAL (
                    SELECT 
                        jsonb_agg(jsonb_build_object(
                            'type', 'PULL_REQUEST',
                            'github_number', pr.number,
                            'github_status', pr.status,
                            'github_title', pr.title,
                            'github_html_url', pr.html_url,
                            'github_body', pr.body,
                            'github_author_id', pr.author_id,
                            'github_author_login', pr.author_login,
                            'github_author_html_url', pr.author_html_url,
                            'github_author_avatar_url', user_avatar_url(pr.author_id, pr.author_avatar_url),
                            'is_mine', c.contributor_id = pr.author_id,
                            'repo_id', pr.repo_id,
                            'repo_owner', pr.repo_owner_login,
                            'repo_name', pr.repo_name,
                            'repo_html_url', pr.repo_html_url
                        )
                    ) as links
                    FROM
                        indexer_exp.github_pull_requests_closing_issues pr_ci
                    INNER JOIN indexer_exp.github_pull_requests pr ON pr.id = pr_ci.pull_request_id
                    WHERE 
                        pr_ci.issue_id = c.issue_id
                    GROUP BY 
                        c.id
                ) AS closing_pull_requests ON TRUE
                LEFT JOIN LATERAL (
                    SELECT 
                        jsonb_agg(jsonb_build_object(
                            'type', 'PULL_REQUEST',
                            'github_number', pr.number,
                            'github_status', pr.status,
                            'github_title', pr.title,
                            'github_html_url', pr.html_url,
                            'github_body', pr.body,
                            'github_author_id', pr.author_id,
                            'github_author_login', pr.author_login,
                            'github_author_html_url', pr.author_html_url,
                            'github_author_avatar_url', user_avatar_url(pr.author_id, pr.author_avatar_url),
                            'is_mine', c.contributor_id = pr.author_id,
                            'repo_id', pr.repo_id,
                            'repo_owner', pr.repo_owner_login,
                            'repo_name', pr.repo_name,
                            'repo_html_url', pr.repo_html_url
                        )
                    ) as links
                    FROM indexer_exp.github_code_reviews cr
                    INNER JOIN indexer_exp.github_pull_requests pr on cr.pull_request_id = pr.id
                    WHERE
                        cr.id = c.code_review_id
                    GROUP BY 
                        c.id
                ) AS reviewed_pull_requests ON TRUE
                LEFT JOIN LATERAL (
                    SELECT 
                        jsonb_agg(r.id) as ids
                    FROM
                        rewards r
                    JOIN reward_items ri ON ri.reward_id = r.id 
                    WHERE
                        ri.id = COALESCE(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id) AND
                        c.contributor_id = r.recipient_id AND
                        r.project_id = p.id
                ) AS rewards ON TRUE
                WHERE 
                    c.id = :contributionId AND
                    p.id = :projectId
            """, nativeQuery = true)
    Optional<ContributionDetailsQueryEntity> findContributionById(UUID projectId, String contributionId);
}
