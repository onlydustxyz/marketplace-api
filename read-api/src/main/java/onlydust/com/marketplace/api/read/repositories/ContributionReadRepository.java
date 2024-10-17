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

import java.util.UUID;

public interface ContributionReadRepository extends Repository<ContributionReadEntity, Long> {

    @Query(value = """
            SELECT c.*,
                   (select jsonb_build_object(
                                   'id', gr.id,
                                   'owner', gr.owner_login,
                                   'name', gr.name,
                                   'description', gr.description,
                                   'htmlUrl', gr.html_url)
                    from indexer_exp.github_repos gr
                    where gr.id = c.repo_id)                           as github_repo,
            
                   (select cd.contributor
                    from bi.p_contributor_global_data cd
                    where cd.contributor_id = c.github_author_id)      as github_author,
            
                   (select jsonb_build_object(
                                   'id', p.id,
                                   'slug', p.slug,
                                   'name', p.name,
                                   'logoUrl', p.logo_url)
                    from projects p
                    where p.id = c.project_id)                         as project,
            
                   (select jsonb_agg(cd.contributor)
                    from bi.p_contributor_global_data cd
                    where cd.contributor_id = any (c.contributor_ids)) as contributors,
            
                   (select jsonb_agg(cd.contributor)
                    from bi.p_contributor_global_data cd
                    where cd.contributor_id = any (c.applicant_ids))   as applicants,
            
                   (select jsonb_agg(jsonb_build_object('name', gl.name,
                                                        'description', gl.description))
                    from indexer_exp.github_labels gl
                    where gl.id = any (c.github_label_ids))            as github_labels,
            
                   (select jsonb_agg(jsonb_build_object('id', l.id,
                                                        'slug', l.slug,
                                                        'name', l.name,
                                                        'logoUrl', l.logo_url,
                                                        'bannerUrl', l.banner_url))
                    from languages l
                    where l.id = any (c.language_ids))                 as languages,
            
                   (select jsonb_agg(jsonb_build_object('type', 'ISSUE',
                                                        'githubId', i.id,
                                                        'githubNumber', i.number,
                                                        'githubStatus', i.status,
                                                        'githubTitle', i.title,
                                                        'githubHtmlUrl', i.html_url))
                    from indexer_exp.github_issues i
                    where i.id = any (c.closing_issue_ids))            as linked_issues
            from (select c.contribution_uuid                       as contribution_uuid,
                         c.contribution_type                       as contribution_type,
                         c.repo_id                                 as repo_id,
                         c.github_number                           as github_number,
                         c.github_status                           as github_status,
                         c.github_title                            as github_title,
                         c.github_html_url                         as github_html_url,
                         c.github_body                             as github_body,
                         c.activity_status                         as activity_status,
                         c.github_author_id                        as github_author_id,
                         c.project_id                              as project_id,
                         c.project_slug                            as project_slug,
                         c.updated_at                              as last_updated_at,
                         c.created_at                              as created_at,
                         c.completed_at                            as completed_at,
                         c.contributor_ids                         as contributor_ids,
                         c.applicant_ids                           as applicant_ids,
                         c.language_ids                            as language_ids,
                         c.closing_issue_ids                       as closing_issue_ids,
                         c.github_label_ids                        as github_label_ids,
                         coalesce(rd.total_rewarded_usd_amount, 0) as total_rewarded_usd_amount
                  from bi.p_contribution_data c
                           left join bi.p_contribution_reward_data rd on rd.contribution_uuid = c.contribution_uuid) c
            where (coalesce(:ids) is null or c.contribution_uuid = any (:ids))
              and (coalesce(:types) is null or c.contribution_type = any (cast(:types as indexer_exp.contribution_type[])))
              and (coalesce(:projectIds) is null or c.project_id = any (:projectIds))
              and (coalesce(:projectSlugs) is null or c.project_slug = any (:projectSlugs))
              and (coalesce(:statuses) is null or c.activity_status = any (:statuses))
              and (coalesce(:repoIds) is null or c.repo_id = any (:repoIds))
              and (coalesce(:contributorIds) is null or c.contributor_ids && :contributorIds)
              and (coalesce(:hasBeenRewarded) is null or :hasBeenRewarded = (c.total_rewarded_usd_amount > 0))
            """, countQuery = """
            select count(c.contribution_uuid)
            from bi.p_contribution_data c
                     left join bi.p_contribution_reward_data rd on rd.contribution_uuid = c.contribution_uuid
            where
                (coalesce(:ids) is null or c.contribution_uuid = any (:ids)) and
                (coalesce(:types) is null or c.contribution_type = any (cast(:types as indexer_exp.contribution_type[]))) and
                (coalesce(:projectIds) is null or c.project_id = any (:projectIds)) and
                (coalesce(:projectSlugs) is null or c.project_slug = any (:projectSlugs)) and
                (coalesce(:statuses) is null or c.activity_status = any (:statuses)) and
                (coalesce(:repoIds) is null or c.repo_id = any (:repoIds)) and
                (coalesce(:contributorIds) is null or c.contributor_ids && :contributorIds) and
                (coalesce(:hasBeenRewarded) is null or :hasBeenRewarded = (rd.total_rewarded_usd_amount > 0))
            """, nativeQuery = true)
    Page<ContributionReadEntity> findAll(UUID[] ids,
                                         String[] types,
                                         UUID[] projectIds,
                                         String[] projectSlugs,
                                         String[] statuses,
                                         Long[] repoIds,
                                         Long[] contributorIds,
                                         Boolean hasBeenRewarded,
                                         Pageable pageable);

    default Page<ContributionReadEntity> findAll(ContributionsQueryParams q) {
        return findAll(
                q.getIds() == null ? null : q.getIds().toArray(UUID[]::new),
                q.getTypes() == null ? null : q.getTypes().stream().map(Enum::name).toArray(String[]::new),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getProjectSlugs() == null ? null : q.getProjectSlugs().toArray(String[]::new),
                q.getStatuses() == null ? null : q.getStatuses().stream().map(Enum::name).toArray(String[]::new),
                q.getRepoIds() == null ? null : q.getRepoIds().toArray(Long[]::new),
                q.getContributorIds() == null ? null : q.getContributorIds().toArray(Long[]::new),
                q.getHasBeenRewarded(),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(q.getSortDirection() == SortDirection.DESC ? Sort.Direction.DESC :
                        Sort.Direction.ASC, getSortProperty(q.getSort()))));
    }

    static String getSortProperty(ContributionsSortEnum sort) {
        return sort == null ? "created_at" : switch (sort) {
            case CREATED_AT -> "created_at";
            case TYPE -> "contribution_type";
        };
    }
}
