package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionViewEntity;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.kernel.model.ProjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContributionViewEntityRepository extends JpaRepository<ContributionViewEntity, String> {

    @Query(value = """
            SELECT
                c.id,
                c.created_at,
                c.completed_at,
                coalesce(c.completed_at, c.created_at) as last_updated_at,
                c.type,
                c.status,
                c.contributor_id,
                c.contributor_login,
                c.contributor_html_url,
                u.id IS NOT NULL as contributor_is_registered,
                coalesce(contributor_upi.avatar_url, c.contributor_avatar_url) as contributor_avatar_url,
                c.github_number,
                c.github_status,
                c.github_title,
                c.github_html_url,
                c.github_body,
                c.github_author_id,
                c.github_author_login,
                c.github_author_html_url,
                coalesce(author_upi.avatar_url, c.github_author_avatar_url) as github_author_avatar_url,
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
                rewards.ids as reward_ids,
                c.pr_review_state,
                contrib_ecosystems.ecosystem_ids as ecosystem_ids,
                contrib_languages.language_ids as language_ids
            FROM
                indexer_exp.contributions c
            INNER JOIN indexer_exp.github_repos gr on c.repo_id = gr.id and gr.visibility = 'PUBLIC'
            INNER JOIN public.project_github_repos pgr on pgr.github_repo_id = gr.id
            INNER JOIN public.projects p on p.id = pgr.project_id
            LEFT JOIN iam.users u on u.github_user_id = c.contributor_id
            LEFT JOIN user_profile_info contributor_upi on contributor_upi.id = u.id
            LEFT JOIN iam.users author on author.github_user_id = c.github_author_id
            LEFT JOIN user_profile_info author_upi on author_upi.id = author.id
            LEFT JOIN LATERAL (
                SELECT array_agg(distinct peco.ecosystem_id) as ecosystem_ids
                FROM projects_ecosystems peco
                WHERE peco.project_id = p.id
            ) AS contrib_ecosystems ON TRUE
            LEFT JOIN LATERAL (
                SELECT array_agg(distinct langext.language_id) as language_ids
                FROM language_file_extensions langext
                WHERE langext.extension = ANY(c.main_file_extensions)
            ) AS contrib_languages ON TRUE
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
                        'github_author_avatar_url', coalesce(author_upi.avatar_url, i.author_avatar_url),
                        'is_mine', :callerGithubUserId = i.author_id,
                        'repo_id', i.repo_id,
                        'repo_owner', i.repo_owner_login,
                        'repo_name', i.repo_name,
                        'repo_html_url', i.repo_html_url
                    )
                ) as links
                FROM
                    indexer_exp.github_pull_requests_closing_issues pr_ci
                    INNER JOIN indexer_exp.github_issues i ON i.id = pr_ci.issue_id
                    LEFT JOIN iam.users author ON author.github_user_id = i.author_id
                    LEFT JOIN user_profile_info author_upi ON author_upi.id = author.id
                WHERE
                    pr_ci.pull_request_id = c.pull_request_id
                GROUP BY
                    c.id
            ) AS closing_issues ON c.pull_request_id IS NOT NULL
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
                        'github_author_avatar_url', coalesce(author_upi.avatar_url, pr.author_avatar_url),
                        'is_mine', :callerGithubUserId = pr.author_id,
                        'repo_id', pr.repo_id,
                        'repo_owner', pr.repo_owner_login,
                        'repo_name', pr.repo_name,
                        'repo_html_url', pr.repo_html_url
                    )
                ) as links
                FROM
                    indexer_exp.github_pull_requests_closing_issues pr_ci
                    INNER JOIN indexer_exp.github_pull_requests pr ON pr.id = pr_ci.pull_request_id
                    LEFT JOIN iam.users author ON author.github_user_id = pr.author_id
                    LEFT JOIN user_profile_info author_upi ON author_upi.id = author.id
                WHERE
                    pr_ci.issue_id = c.issue_id
                GROUP BY
                    c.id
            ) AS closing_pull_requests ON c.issue_id IS NOT NULL
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
                        'github_author_avatar_url', coalesce(author_upi.avatar_url, pr.author_avatar_url),
                        'is_mine', :callerGithubUserId = pr.author_id,
                        'repo_id', pr.repo_id,
                        'repo_owner', pr.repo_owner_login,
                        'repo_name', pr.repo_name,
                        'repo_html_url', pr.repo_html_url
                    )
                ) as links
                FROM indexer_exp.github_code_reviews cr
                    INNER JOIN indexer_exp.github_pull_requests pr on cr.pull_request_id = pr.id
                    LEFT JOIN iam.users author ON author.github_user_id = pr.author_id
                    LEFT JOIN user_profile_info author_upi ON author_upi.id = author.id
                WHERE
                    cr.id = c.code_review_id
                GROUP BY
                    c.id
            ) AS reviewed_pull_requests ON c.code_review_id IS NOT NULL
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
                c.contributor_id IS NOT NULL AND
                (COALESCE(:contributorIds) IS NULL OR c.contributor_id IN (:contributorIds)) AND
                (COALESCE(:projectIds) IS NULL OR p.id IN (:projectIds)) AND
                (COALESCE(:repoIds) IS NULL OR c.repo_id IN (:repoIds)) AND
                (COALESCE(:types) IS NULL OR CAST(c.type AS TEXT) IN (:types)) AND
                (COALESCE(:statuses) IS NULL OR CAST(c.status AS TEXT) IN (:statuses)) AND
                (array_length(:ecosystemIds, 1) IS NULL OR contrib_ecosystems.ecosystem_ids && :ecosystemIds) AND
                (array_length(:languageIds, 1) IS NULL OR contrib_languages.language_ids && :languageIds) AND
                (p.visibility = 'PUBLIC' OR (:includePrivateProjects IS TRUE AND c.contributor_id = :callerGithubUserId)) AND
                (:fromDate IS NULL OR coalesce(c.completed_at, c.created_at) >= to_date(cast(:fromDate as text), 'YYYY-MM-DD')) AND
                (:toDate IS NULL OR coalesce(c.completed_at, c.created_at) < to_date(cast(:toDate as text), 'YYYY-MM-DD') + 1)
            """, nativeQuery = true)
    Page<ContributionViewEntity> findContributions(Long callerGithubUserId,
                                                   List<Long> contributorIds,
                                                   List<UUID> projectIds,
                                                   List<Long> repoIds,
                                                   List<String> types,
                                                   List<String> statuses,
                                                   UUID[] languageIds,
                                                   UUID[] ecosystemIds,
                                                   boolean includePrivateProjects,
                                                   String fromDate,
                                                   String toDate,
                                                   Pageable pageable);

     @Query(value = """
            SELECT
                c.id,
                c.created_at,
                c.completed_at,
                coalesce(c.completed_at, c.created_at) as last_updated_at,
                c.type,
                c.status,
                c.contributor_id,
                c.contributor_login,
                c.contributor_html_url,
                u.id IS NOT NULL as contributor_is_registered,
                coalesce(contributor_upi.avatar_url, c.contributor_avatar_url) as contributor_avatar_url,
                c.github_number,
                c.github_status,
                c.github_title,
                c.github_html_url,
                c.github_body,
                c.github_author_id,
                c.github_author_login,
                c.github_author_html_url,
                coalesce(author_upi.avatar_url, c.github_author_avatar_url) as github_author_avatar_url,
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
                rewards.ids as reward_ids,
                c.pr_review_state,
                contrib_ecosystems.ecosystem_ids as ecosystem_ids,
                contrib_languages.language_ids as language_ids,
                gc.contribution_uuid grouped_id
            FROM
                indexer_exp.contributions c
            LEFT JOIN indexer_exp.grouped_contributions gc on COALESCE(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id) =
                                                                          COALESCE(CAST(gc.pull_request_id AS TEXT), CAST(gc.issue_id AS TEXT), gc.code_review_id)
            INNER JOIN indexer_exp.github_repos gr on c.repo_id = gr.id and gr.visibility = 'PUBLIC'
            INNER JOIN public.project_github_repos pgr on pgr.github_repo_id = gr.id
            INNER JOIN public.projects p on p.id = pgr.project_id
            LEFT JOIN iam.users u on u.github_user_id = c.contributor_id
            LEFT JOIN user_profile_info contributor_upi on contributor_upi.id = u.id
            LEFT JOIN iam.users author on author.github_user_id = c.github_author_id
            LEFT JOIN user_profile_info author_upi on author_upi.id = author.id
            LEFT JOIN LATERAL (
                SELECT array_agg(distinct peco.ecosystem_id) as ecosystem_ids
                FROM projects_ecosystems peco
                WHERE peco.project_id = p.id
            ) AS contrib_ecosystems ON TRUE
            LEFT JOIN LATERAL (
                SELECT array_agg(distinct langext.language_id) as language_ids
                FROM language_file_extensions langext
                WHERE langext.extension = ANY(c.main_file_extensions)
            ) AS contrib_languages ON TRUE
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
                        'github_author_avatar_url', coalesce(author_upi.avatar_url, i.author_avatar_url),
                        'is_mine', :callerGithubUserId = i.author_id,
                        'repo_id', i.repo_id,
                        'repo_owner', i.repo_owner_login,
                        'repo_name', i.repo_name,
                        'repo_html_url', i.repo_html_url
                    )
                ) as links
                FROM
                    indexer_exp.github_pull_requests_closing_issues pr_ci
                    INNER JOIN indexer_exp.github_issues i ON i.id = pr_ci.issue_id
                    LEFT JOIN iam.users author ON author.github_user_id = i.author_id
                    LEFT JOIN user_profile_info author_upi ON author_upi.id = author.id
                WHERE
                    pr_ci.pull_request_id = c.pull_request_id
                GROUP BY
                    c.id
            ) AS closing_issues ON c.pull_request_id IS NOT NULL
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
                        'github_author_avatar_url', coalesce(author_upi.avatar_url, pr.author_avatar_url),
                        'is_mine', :callerGithubUserId = pr.author_id,
                        'repo_id', pr.repo_id,
                        'repo_owner', pr.repo_owner_login,
                        'repo_name', pr.repo_name,
                        'repo_html_url', pr.repo_html_url
                    )
                ) as links
                FROM
                    indexer_exp.github_pull_requests_closing_issues pr_ci
                    INNER JOIN indexer_exp.github_pull_requests pr ON pr.id = pr_ci.pull_request_id
                    LEFT JOIN iam.users author ON author.github_user_id = pr.author_id
                    LEFT JOIN user_profile_info author_upi ON author_upi.id = author.id
                WHERE
                    pr_ci.issue_id = c.issue_id
                GROUP BY
                    c.id
            ) AS closing_pull_requests ON c.issue_id IS NOT NULL
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
                        'github_author_avatar_url', coalesce(author_upi.avatar_url, pr.author_avatar_url),
                        'is_mine', :callerGithubUserId = pr.author_id,
                        'repo_id', pr.repo_id,
                        'repo_owner', pr.repo_owner_login,
                        'repo_name', pr.repo_name,
                        'repo_html_url', pr.repo_html_url
                    )
                ) as links
                FROM indexer_exp.github_code_reviews cr
                    INNER JOIN indexer_exp.github_pull_requests pr on cr.pull_request_id = pr.id
                    LEFT JOIN iam.users author ON author.github_user_id = pr.author_id
                    LEFT JOIN user_profile_info author_upi ON author_upi.id = author.id
                WHERE
                    cr.id = c.code_review_id
                GROUP BY
                    c.id
            ) AS reviewed_pull_requests ON c.code_review_id IS NOT NULL
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
                c.contributor_id = :contributorId AND
                gc.contribution_uuid = :contributionUUID
            GROUP BY gc.contribution_uuid
            """, nativeQuery = true)
     Optional<ContributionViewEntity> findContributions(UUID contributionUUID, Long contributorId);



    @Query(value = """
            SELECT
                count(distinct c.id)
            FROM
                indexer_exp.contributions c
            INNER JOIN indexer_exp.github_repos gr on c.repo_id = gr.id and gr.visibility = 'PUBLIC'
            INNER JOIN public.project_github_repos pgr on pgr.github_repo_id = gr.id
            WHERE 
                c.contributor_id = :contributorId AND
                pgr.project_id = :projectId
            """, nativeQuery = true)
    int countBy(Long contributorId, UUID projectId);

    @Query(value = """
            SELECT
                c.contribution_uuid
            FROM
                indexer_exp.grouped_contributions c
            WHERE
                coalesce(cast(c.issue_id as text), cast(c.pull_request_id as text), c.code_review_id) = :issuePRCodeReviewId
            """, nativeQuery = true)
    UUID getContributionUUID(String issuePRCodeReviewId);

    @Query(nativeQuery = true, value = """
            SELECT c.id,
                   c.created_at,
                   c.completed_at,
                   coalesce(c.completed_at, c.created_at)                         as last_updated_at,
                   c.type,
                   c.status,
                   c.contributor_id,
                   c.contributor_login,
                   c.contributor_html_url,
                   coalesce(contributor_upi.avatar_url, c.contributor_avatar_url) as contributor_avatar_url,
                   c.github_number,
                   c.github_status,
                   c.github_title,
                   c.github_html_url,
                   c.github_body,
                   c.github_author_id,
                   c.github_author_login,
                   c.github_author_html_url,
                   coalesce(author_upi.avatar_url, c.github_author_avatar_url)    as github_author_avatar_url,
                   p.id                                                           as project_id,
                   p.name                                                         as project_name,
                   p.slug                                                         as project_key,
                   p.short_description                                            as project_short_description,
                   p.logo_url                                                     as project_logo_url,
                   p.visibility                                                   as project_visibility,
                   c.repo_id,
                   c.repo_owner_login                                             as repo_owner,
                   c.repo_name,
                   c.repo_html_url,
                   gc.contribution_uuid                                              grouped_id,
                   null contributor_is_registered,
                   COALESCE(closing_issues.links,closing_pull_requests.links, reviewed_pull_requests.links) as links,
                   rewards.ids as reward_ids,
                   c.pr_review_state
            FROM indexer_exp.contributions c
                     LEFT JOIN indexer_exp.grouped_contributions gc
                               on COALESCE(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id) =
                                  COALESCE(CAST(gc.pull_request_id AS TEXT), CAST(gc.issue_id AS TEXT), gc.code_review_id)
                     INNER JOIN indexer_exp.github_repos gr on c.repo_id = gr.id and gr.visibility = 'PUBLIC'
                     INNER JOIN public.project_github_repos pgr on pgr.github_repo_id = gr.id
                     INNER JOIN public.projects p on p.id = pgr.project_id
                     LEFT JOIN iam.users u on u.github_user_id = c.contributor_id
                     LEFT JOIN user_profile_info contributor_upi on contributor_upi.id = u.id
                     LEFT JOIN iam.users author on author.github_user_id = c.github_author_id
                     LEFT JOIN user_profile_info author_upi on author_upi.id = author.id
                     LEFT JOIN LATERAL (
                SELECT array_agg(distinct peco.ecosystem_id) as ecosystem_ids
                FROM projects_ecosystems peco
                WHERE peco.project_id = p.id
                ) AS contrib_ecosystems ON TRUE
                     LEFT JOIN LATERAL (
                SELECT array_agg(distinct langext.language_id) as language_ids
                FROM language_file_extensions langext
                WHERE langext.extension = ANY (c.main_file_extensions)
                ) AS contrib_languages ON TRUE
                     LEFT JOIN LATERAL (
                SELECT jsonb_agg(jsonb_build_object(
                        'type', 'ISSUE',
                        'github_number', i.number,
                        'github_status', i.status,
                        'github_title', i.title,
                        'github_html_url', i.html_url,
                        'github_body', i.body,
                        'github_author_id', i.author_id,
                        'github_author_login', i.author_login,
                        'github_author_html_url', i.author_html_url,
                        'github_author_avatar_url', coalesce(author_upi.avatar_url, i.author_avatar_url),
                        'is_mine', null,
                        'repo_id', i.repo_id,
                        'repo_owner', i.repo_owner_login,
                        'repo_name', i.repo_name,
                        'repo_html_url', i.repo_html_url
                                 )
                       ) as links
                FROM indexer_exp.github_pull_requests_closing_issues pr_ci
                         INNER JOIN indexer_exp.github_issues i ON i.id = pr_ci.issue_id
                         LEFT JOIN iam.users author ON author.github_user_id = i.author_id
                         LEFT JOIN user_profile_info author_upi ON author_upi.id = author.id
                WHERE pr_ci.pull_request_id = c.pull_request_id
                GROUP BY c.id
                ) AS closing_issues ON c.pull_request_id IS NOT NULL
                     LEFT JOIN LATERAL (
                SELECT jsonb_agg(jsonb_build_object(
                        'type', 'PULL_REQUEST',
                        'github_number', pr.number,
                        'github_status', pr.status,
                        'github_title', pr.title,
                        'github_html_url', pr.html_url,
                        'github_body', pr.body,
                        'github_author_id', pr.author_id,
                        'github_author_login', pr.author_login,
                        'github_author_html_url', pr.author_html_url,
                        'github_author_avatar_url', coalesce(author_upi.avatar_url, pr.author_avatar_url),
                        'is_mine', null,
                        'repo_id', pr.repo_id,
                        'repo_owner', pr.repo_owner_login,
                        'repo_name', pr.repo_name,
                        'repo_html_url', pr.repo_html_url
                                 )
                       ) as links
                FROM indexer_exp.github_pull_requests_closing_issues pr_ci
                         INNER JOIN indexer_exp.github_pull_requests pr ON pr.id = pr_ci.pull_request_id
                         LEFT JOIN iam.users author ON author.github_user_id = pr.author_id
                         LEFT JOIN user_profile_info author_upi ON author_upi.id = author.id
                WHERE pr_ci.issue_id = c.issue_id
                GROUP BY c.id
                ) AS closing_pull_requests ON c.issue_id IS NOT NULL
                     LEFT JOIN LATERAL (
                SELECT jsonb_agg(jsonb_build_object(
                        'type', 'PULL_REQUEST',
                        'github_number', pr.number,
                        'github_status', pr.status,
                        'github_title', pr.title,
                        'github_html_url', pr.html_url,
                        'github_body', pr.body,
                        'github_author_id', pr.author_id,
                        'github_author_login', pr.author_login,
                        'github_author_html_url', pr.author_html_url,
                        'github_author_avatar_url', coalesce(author_upi.avatar_url, pr.author_avatar_url),
                        'is_mine', null,
                        'repo_id', pr.repo_id,
                        'repo_owner', pr.repo_owner_login,
                        'repo_name', pr.repo_name,
                        'repo_html_url', pr.repo_html_url
                                 )
                       ) as links
                FROM indexer_exp.github_code_reviews cr
                         INNER JOIN indexer_exp.github_pull_requests pr on cr.pull_request_id = pr.id
                         LEFT JOIN iam.users author ON author.github_user_id = pr.author_id
                         LEFT JOIN user_profile_info author_upi ON author_upi.id = author.id
                WHERE cr.id = c.code_review_id
                GROUP BY c.id
                ) AS reviewed_pull_requests ON c.code_review_id IS NOT NULL
                     LEFT JOIN LATERAL (
                SELECT jsonb_agg(r.id) as ids
                FROM rewards r
                         JOIN reward_items ri ON ri.reward_id = r.id
                WHERE ri.id = COALESCE(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id)
                  AND c.contributor_id = r.recipient_id
                  AND r.project_id = p.id
                ) AS rewards ON TRUE
            WHERE c.contributor_id = :contributorId
              AND gc.contribution_uuid = :contributionUuid
              and p.id = :projectId
            GROUP BY c.id, gc.contribution_uuid, u.id, c.type,
                     c.status,
                     c.contributor_id,
                     c.contributor_login,
                     c.contributor_html_url,
                     c.github_number,
                     c.github_status,
                     c.github_title,
                     c.github_html_url,
                     c.github_body,
                     c.github_author_id,
                     c.github_author_login,
                     c.github_author_html_url,
                     coalesce(contributor_upi.avatar_url, c.contributor_avatar_url),
                     coalesce(author_upi.avatar_url, c.github_author_avatar_url),
                     p.id,
                     p.name,
                     p.slug,
                     p.short_description,
                     p.logo_url,
                     p.visibility,
                     c.repo_id,
                     c.repo_owner_login,
                     c.repo_name,
                     c.repo_html_url,
                     COALESCE(closing_issues.links,closing_pull_requests.links, reviewed_pull_requests.links),
                     rewards.ids,
                     c.pr_review_state""")
    Optional<ContributionViewEntity> findByUuidAndContributorId(UUID projectId, UUID contributionUuid, Long contributorId);
}
