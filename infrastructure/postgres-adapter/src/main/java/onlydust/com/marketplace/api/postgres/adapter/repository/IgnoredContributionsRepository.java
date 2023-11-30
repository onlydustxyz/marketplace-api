package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IgnoredContributionsRepository extends JpaRepository<IgnoredContributionEntity,
        IgnoredContributionEntity.Id> {

    @Query("select c from IgnoredContributionEntity c where c.id.projectId = ?1")
    List<IgnoredContributionEntity> findAllByProjectId(UUID projectId);

    @Modifying
    @Query(value = """
            insert into ignored_contributions (project_id, contribution_id)
               select pgr.project_id, c.id
               from indexer_exp.contributions c
               join indexer_exp.github_repos gr on gr.id = c.repo_id
               join project_github_repos pgr on pgr.github_repo_id = c.repo_id
               join project_details pd on pd.project_id = pgr.project_id
               left join custom_ignored_contributions cic on cic.contribution_id = c.id
                                                           and cic.project_id = pgr.project_id
                                                           and cic.ignored = false
               where
                   c.repo_id in ?1
                   and cic.contribution_id is null
                   and gr.visibility = 'PUBLIC'
                   and (
                       (pd.reward_ignore_contributions_before_date_by_default is not null
                           and c.created_at < pd.reward_ignore_contributions_before_date_by_default)
                    or (pd.reward_ignore_pull_requests_by_default = true and c.type = 'PULL_REQUEST')
                    or (pd.reward_ignore_issues_by_default = true and c.type = 'ISSUE')
                    or (pd.reward_ignore_code_reviews_by_default = true and c.type = 'CODE_REVIEW')
                   )
               on conflict do nothing
               """, nativeQuery = true)
    void addMissingContributions(List<Long> reposIds);


    @Modifying
    @Query(value = """
            delete from ignored_contributions ic
            where (ic.project_id, ic.contribution_id) in (
                select pgr.project_id, c.id
                from indexer_exp.contributions c
                join indexer_exp.github_repos gr on gr.id = c.repo_id
                join project_github_repos pgr on pgr.github_repo_id = c.repo_id
                join project_details pd on pd.project_id = pgr.project_id
                left join custom_ignored_contributions cic on cic.contribution_id = c.id
                                                            and cic.project_id = pgr.project_id
                                                            and cic.ignored = true
                where
                    c.repo_id in ?1
                    and cic.contribution_id is null
                    and gr.visibility = 'PUBLIC'
                    and (
                        (pd.reward_ignore_contributions_before_date_by_default is null
                            or c.created_at >= pd.reward_ignore_contributions_before_date_by_default)
                     and (pd.reward_ignore_pull_requests_by_default = false or c.type != 'PULL_REQUEST')
                     and (pd.reward_ignore_issues_by_default = false or c.type != 'ISSUE')
                     and (pd.reward_ignore_code_reviews_by_default = false or c.type != 'CODE_REVIEW')
                    )
            )
            """, nativeQuery = true)
    void deleteContributionsThatShouldNotBeIgnored(List<Long> reposIds);

    @Modifying
    @Query(value = """
            delete from ignored_contributions ic
            where ic.project_id = ?1 and ic.contribution_id not in (
                select c.id
                from indexer_exp.contributions c
                join indexer_exp.github_repos gr on gr.id = c.repo_id
                join project_github_repos pgr on pgr.project_id = ?1 and pgr.github_repo_id = gr.id
                where gr.visibility = 'PUBLIC'
            )
             """, nativeQuery = true)
    void deleteContributionsThatAreNotPartOfTheProjectAnymore(UUID projectId);
}
