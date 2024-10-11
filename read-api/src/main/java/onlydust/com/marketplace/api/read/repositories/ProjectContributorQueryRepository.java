package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectContributorsQueryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ProjectContributorQueryRepository extends Repository<ProjectContributorsQueryEntity, Long> {

    @Query(value = """
            select ga.id                                               as id,
                   ga.login                                            as login,
                   user_avatar_url(ga.id, ga.avatar_url)               as avatar_url,
                   pc.completed_contribution_count                     as contribution_count,
                   u.github_user_id is not null                        as is_registered,
                   (select count(distinct r.id)
                    from rewards r
                    where r.project_id = :projectId
                      and r.recipient_id = ga.id)                      as reward_count,
                   coalesce(to_rewards_stats.total_count, 0)           as to_reward_count,
                   to_rewards_stats.pull_request_count                 as prs_to_reward,
                   to_rewards_stats.issue_count                        as issues_to_reward,
                   to_rewards_stats.code_review_count                  as code_reviews_to_reward,
            
                   coalesce(totals_earned.total_dollars_equivalent, 0) as earned,
                   totals_earned.totals_earned_per_currency            as totals_earned,
            
                   hc.contributor_github_user_id is not null           as is_hidden
            
            from projects_contributors pc
                     join indexer_exp.github_accounts ga on ga.id = pc.github_user_id
                     left join iam.users u on u.github_user_id = ga.id
            
                     left join hidden_contributors hc
                               on hc.contributor_github_user_id = pc.github_user_id and 
                                  hc.project_id = :projectId and
                                  hc.project_lead_id = :projectLeadId and
                                  :projectLeadId is not null
            
                     left join (select count(distinct c.id)                                          as total_count,
                                       count(distinct c.id) filter ( where c.type = 'PULL_REQUEST' ) as pull_request_count,
                                       count(distinct c.id) filter ( where c.type = 'CODE_REVIEW' )  as code_review_count,
                                       count(distinct c.id) filter ( where c.type = 'ISSUE' )        as issue_count,
                                       pgr.project_id                                                as project_id,
                                       c.contributor_id                                              as contributor_id
                                from project_github_repos pgr
                                         join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                                         join indexer_exp.contributions c on c.repo_id = gr.id
                                         left join reward_items ri on ri.id = coalesce(cast(c.pull_request_id as text),
                                                                                       cast(c.issue_id as text),
                                                                                       c.code_review_id) and
                                                                      ri.recipient_id = c.contributor_id
                                         left join ignored_contributions ic
                                                   on ic.contribution_id = c.id and ic.project_id = :projectId
                                where gr.visibility = 'PUBLIC'
                                  and c.status = 'COMPLETED'
                                  and ri.id is null
                                  and ic.project_id is null
                                group by pgr.project_id, c.contributor_id) to_rewards_stats
                               on to_rewards_stats.contributor_id = ga.id and to_rewards_stats.project_id = pc.project_id
            
                     left join (select user_rewards.recipient_id                  as recipient_id,
                                       user_rewards.project_id                    as project_id,
                                       sum(user_rewards.total_dollars_equivalent) as total_dollars_equivalent,
                                       jsonb_agg(jsonb_build_object(
                                               'total_amount', user_rewards.total_amount,
                                               'total_dollars_equivalent', user_rewards.total_dollars_equivalent,
                                               'currency_id', user_rewards.currency_id,
                                               'currency_code', user_rewards.currency_code,
                                               'currency_name', user_rewards.currency_name,
                                               'currency_decimals', user_rewards.currency_decimals,
                                               'currency_logo_url', user_rewards.currency_logo_url
                                                 ))                               as totals_earned_per_currency
                                from (select r.project_id                                as project_id,
                                             r.recipient_id                              as recipient_id,
                                             sum(r.amount)                               as total_amount,
                                             coalesce(sum(r.amount_usd_equivalent), 0) as total_dollars_equivalent,
                                             c.id                                        as currency_id,
                                             c.code                                      as currency_code,
                                             c.name                                      as currency_name,
                                             c.decimals                                  as currency_decimals,
                                             c.logo_url                                  as currency_logo_url
                                      from accounting.reward_statuses r
                                               join currencies c on c.id = r.currency_id
                                      group by r.recipient_id, c.id, r.project_id) as user_rewards
                                group by user_rewards.recipient_id, user_rewards.project_id) totals_earned
                         on totals_earned.recipient_id = ga.id and totals_earned.project_id = pc.project_id
            
            where pc.project_id = :projectId
              and (:login is null or ga.login ilike '%' || :login || '%')
              and (hc.contributor_github_user_id is null or :showHidden)
            """,
            countQuery = """
                    select count(ga.id)
                    from projects_contributors pc
                             join indexer_exp.github_accounts ga on ga.id = pc.github_user_id
                             left join hidden_contributors hc
                                       on hc.contributor_github_user_id = pc.github_user_id and 
                                          hc.project_id = :projectId and
                                          hc.project_lead_id = :projectLeadId and
                                          :projectLeadId is not null
                    where pc.project_id = :projectId
                      and (:login is null or ga.login ilike '%' || :login || '%')
                      and (hc.contributor_github_user_id is null or :showHidden)
                    """, nativeQuery = true)
    Page<ProjectContributorsQueryEntity> findProjectContributors(final UUID projectId,
                                                                 final String login,
                                                                 final UUID projectLeadId,
                                                                 final Boolean showHidden,
                                                                 final Pageable pageable);

    @Query(value = """
            select exists(select 1
                          from hidden_contributors hc
                          where hc.project_id = :projectId)
            """, nativeQuery = true)
    boolean hasHiddenContributors(final UUID projectId);
}
