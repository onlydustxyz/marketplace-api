package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.bi.ContributorReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface ContributorReadRepository extends Repository<ContributorReadEntity, Long> {

    @Query(value = """
            SELECT c.contributor_id                                  as contributor_id,
                   c.contributor_login                               as contributor_login,
                   c.contributor_country                             as contributor_country,
                   c.contributor                                     as contributor,
                   c.projects                                        as projects,
                   c.categories                                      as categories,
                   c.languages                                       as languages,
                   c.ecosystems                                      as ecosystems,
                   c.maintained_projects                             as maintained_projects,
                   c.first_project_name                              as first_project_name,
                   -- /// filtered & computed data /// --
                   (select jsonb_agg(jsonb_build_object('id', pcl.id, 'slug', pcl.slug, 'name', pcl.name))
                    from contributor_project_contributor_labels cpcl
                             join project_contributor_labels pcl on pcl.id = cpcl.label_id
                             join projects p on p.id = pcl.project_id
                    where cpcl.github_user_id = c.contributor_id)    as project_contributor_labels,
                   cd.repos                                          as repos,
                   coalesce(sum(rd.total_rewarded_usd_amount), 0)    as total_rewarded_usd_amount,
                   coalesce(sum(rd.reward_count), 0)                 as reward_count,
                   coalesce(sum(cd.completed_contribution_count), 0) as completed_contribution_count,
                   coalesce(sum(cd.completed_issue_count), 0)        as completed_issue_count,
                   coalesce(sum(cd.completed_pr_count), 0)           as completed_pr_count,
                   coalesce(sum(cd.completed_code_review_count), 0)  as completed_code_review_count,
                   coalesce(sum(cd.in_progress_issue_count), 0)      as in_progress_issue_count,
                   coalesce(sum(ad.pending_application_count), 0)    as pending_application_count
            
            FROM bi.p_contributor_global_data c
                     JOIN bi.p_contributor_reward_data crd ON crd.contributor_id = c.contributor_id
                     JOIN bi.p_contributor_application_data cad ON cad.contributor_id = c.contributor_id
            
                     LEFT JOIN (select cd_per_repo.contributor_id                                 as contributor_id,
                                       jsonb_agg(cd_per_repo.repo)                                as repos,
                                       coalesce(sum(cd_per_repo.completed_contribution_count), 0) as completed_contribution_count,
                                       coalesce(sum(cd_per_repo.completed_issue_count), 0)        as completed_issue_count,
                                       coalesce(sum(cd_per_repo.completed_pr_count), 0)           as completed_pr_count,
                                       coalesce(sum(cd_per_repo.completed_code_review_count), 0)  as completed_code_review_count,
                                       coalesce(sum(cd_per_repo.in_progress_issue_count), 0)      as in_progress_issue_count
                                from (select cd.contributor_id                                                                 as contributor_id,
                                             (select jsonb_build_object('id', gr.id,
                                                                        'owner', gr.owner_login,
                                                                        'name', gr.name,
                                                                        'description', gr.description,
                                                                        'htmlUrl', gr.html_url,
                                                                        'stars', gr.stars_count,
                                                                        'forkCount', gr.forks_count,
                                                                        'contributorContributionCount', count(cd.contribution_uuid),
                                                                        'topGithubLanguages', coalesce((select jsonb_agg(to_jsonb(l.name))
                                                                                                        from (select grl.language as name
                                                                                                              from indexer_exp.github_repo_languages grl
                                                                                                              where grl.repo_id = gr.id
                                                                                                              order by grl.line_count desc
                                                                                                              limit 3) l), cast('[]' as jsonb))) as repo
                                              from indexer_exp.github_repos gr
                                              where gr.id = cd.repo_id)                                                        as repo,
                                             count(cd.contribution_uuid) filter ( where cd.contribution_status = 'COMPLETED' ) as completed_contribution_count,
                                             sum(cd.is_issue) filter ( where cd.contribution_status = 'COMPLETED' )            as completed_issue_count,
                                             sum(cd.is_pr) filter ( where cd.contribution_status = 'COMPLETED' )               as completed_pr_count,
                                             sum(cd.is_code_review) filter ( where cd.contribution_status = 'COMPLETED' )      as completed_code_review_count,
                                             sum(cd.is_issue) filter ( where cd.contribution_status = 'IN_PROGRESS' )          as in_progress_issue_count
                                      from bi.p_per_contributor_contribution_data cd
                                      where (:onlyDustContributionsOnly is false or cd.project_id is not null)
                                      group by cd.contributor_id, cd.repo_id) cd_per_repo
                                group by cd_per_repo.contributor_id) cd on cd.contributor_id = c.contributor_id
            
                     LEFT JOIN (select rd.contributor_id,
                                       count(rd.reward_id)             as reward_count,
                                       coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount
                                from bi.p_reward_data rd
                                group by rd.contributor_id) rd on rd.contributor_id = c.contributor_id
            
                     LEFT JOIN (select ad.contributor_id,
                                       count(ad.application_id) filter ( where ad.status = 'PENDING' ) as pending_application_count
                                from bi.p_application_data ad
                                group by ad.contributor_id) ad on ad.contributor_id = c.contributor_id
            
            WHERE c.contributor_id = :contributorId
            GROUP BY c.contributor_id,
                     c.contributor_login,
                     c.contributor_country,
                     c.contributor,
                     c.projects,
                     c.categories,
                     c.languages,
                     c.ecosystems,
                     c.maintained_projects,
                     c.first_project_name,
                     cd.repos
            """
            , nativeQuery = true)
    Optional<ContributorReadEntity> findById(@NonNull Long contributorId,
                                             @NonNull Boolean onlyDustContributionsOnly);

}
