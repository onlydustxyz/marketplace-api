package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.contract.model.ContributionsQueryParams;
import onlydust.com.marketplace.api.contract.model.ContributionsSortEnum;
import onlydust.com.marketplace.api.contract.model.SortDirection;
import onlydust.com.marketplace.api.read.entities.bi.ContributionReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface ContributionReadRepository extends Repository<ContributionReadEntity, Long> {

    @Query(value = """
            SELECT c.contribution_id                                                                                   as contribution_id,
                   c.contribution_type                                                                                 as contribution_type,
                   jsonb_build_object(
                           'id', gr.id,
                           'owner', gr.owner_login,
                           'name', gr.name,
                           'description', gr.description,
                           'htmlUrl', gr.html_url
                   )                                                                                                   as github_repo,
                   jsonb_build_object(
                           'githubUserId', author.github_user_id,
                           'login', author.login,
                           'avatarUrl', author.avatar_url
                   )                                                                                                   as github_author,
                   c.github_id                                                                                         as github_id,
                   c.github_number                                                                                     as github_number,
                   c.github_status                                                                                     as github_status,
                   c.github_title                                                                                      as github_title,
                   c.github_html_url                                                                                   as github_html_url,
                   c.github_body                                                                                       as github_body,
                   jsonb_agg(jsonb_build_object(
                           'name', gl.name,
                           'description', gl.description
                             )) filter ( where gl.id is not null )                                                     as github_labels,
                   c.updated_at                                                                                        as last_updated_at,
                   c.created_at                                                                                        as created_at,
                   c.completed_at                                                                                      as completed_at,
                   case
                       when c.is_issue = 1 then
                           case
                               when c.github_status = 'OPEN' AND c.assignee_ids is null then 'NOT_ASSIGNED'
                               when c.github_status = 'OPEN' AND c.assignee_ids is not null then 'IN_PROGRESS'
                               else 'DONE'
                               end
                       when c.is_pr = 1 then
                           case
                               when c.github_status = 'DRAFT' then 'IN_PROGRESS'
                               when c.github_status = 'OPEN' then 'TO_REVIEW'
                               else 'DONE'
                               end
                       when c.is_code_review = 1 then
                           case
                               when c.github_code_review_state in ('PENDING_REVIEWER', 'UNDER_REVIEW') then 'IN_PROGRESS'
                               else 'DONE'
                               end
                       end                                                                                             as activity_status,
                   jsonb_build_object(
                           'id', p.id,
                           'slug', p.slug,
                           'name', p.name,
                           'logoUrl', p.logo_url
                   )                                                                                                   as project,
                   jsonb_agg(distinct contributor.contributor) filter ( where contributor.contributor_id is not null ) as contributors,
                   jsonb_agg(distinct applicant.contributor) filter ( where applicant.contributor_id is not null )     as applicants,
                   jsonb_agg(distinct jsonb_build_object('id', l.id,
                                                         'slug', l.slug,
                                                         'name', l.name,
                                                         'logoUrl', l.logo_url,
                                                         'bannerUrl', l.banner_url)) filter ( where l.id is not null ) as languages,
                   jsonb_agg(distinct jsonb_build_object(
                           'type', 'ISSUE',
                           'githubId', i.id,
                           'githubNumber', i.number,
                           'githubStatus', i.status,
                           'githubTitle', i.title,
                           'githubHtmlUrl', i.html_url
                                      ))
                   filter ( where i.id is not null )                                                                   as linked_issues,
                   rd.usd_amount                                                                                       as total_rewarded_usd_amount
            FROM bi.p_contribution_data c
                     join indexer_exp.github_repos gr on gr.id = c.repo_id
                     join iam.all_users author on author.github_user_id = c.github_author_id
                     join bi.p_contributor_global_data contributor on contributor.contributor_id = c.contributor_id
                     join projects p on p.id = c.project_id
                     left join languages l ON l.id = any (c.language_ids)
                     left join indexer_exp.github_labels gl on gl.id = any (c.github_label_ids)
                     left join applications a on a.issue_id = c.github_id
                     left join bi.p_contributor_global_data applicant on applicant.contributor_id = a.applicant_id
                     left join indexer_exp.github_pull_requests_closing_issues prci on prci.pull_request_id = c.github_id
                     left join indexer_exp.github_issues i on i.id = prci.issue_id
                     left join bi.p_contribution_reward_data rd on rd.contribution_id = c.contribution_id
            group by c.contribution_id, gr.id, author.github_user_id, author.login, author.avatar_url, p.id
            """,
            nativeQuery = true)
    Page<ContributionReadEntity> findAll(Pageable pageable);

    default Page<ContributionReadEntity> findAll(ContributionsQueryParams q) {
        return findAll(
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        getSortProperty(q.getSort())))
        );
    }

    static String getSortProperty(ContributionsSortEnum sort) {
        return sort == null ? "created_at" : switch (sort) {
            case CREATED_AT -> "created_at";
            case TYPE -> "contribution_type";
        };
    }
}
