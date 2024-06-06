DROP MATERIALIZED VIEW global_users_ranks;

CREATE MATERIALIZED VIEW global_users_ranks AS
with ranks as (select coalesce(c.github_user_id, rr.github_user_id, rs.github_user_id) as github_user_id,
                      rank() over (order by c.rank)                                    as contributions_rank,
                      rank() over (order by rs.rank)                                   as rewards_sent_rank,
                      rank() over (order by rr.rank)                                   as rewards_received_rank,
                      c.contributions_count                                            as contribution_count,
                      c.projects_count                                                 as contributed_project_count,
                      rr.reward_count                                                  as reward_count
               from users_rank_per_contribution c
                        full join users_rank_per_reward_received rr on rr.github_user_id = c.github_user_id
                        full join users_rank_per_reward_sent rs on rs.github_user_id = c.github_user_id),
     max_ranks as (select max(contributions_rank)    as contributions_rank,
                          max(rewards_sent_rank)     as rewards_sent_rank,
                          max(rewards_received_rank) as rewards_received_rank
                   from ranks),
     normalized_ranks as (select r.github_user_id                                                     as github_user_id,
                                 round(100.0 * r.contributions_rank / mr.contributions_rank, 2)       as contributions_rank,
                                 round(100.0 * r.rewards_sent_rank / mr.rewards_sent_rank, 2)         as rewards_sent_rank,
                                 round(100.0 * r.rewards_received_rank / mr.rewards_received_rank, 2) as rewards_received_rank,
                                 r.contribution_count,
                                 r.contributed_project_count,
                                 r.reward_count
                          from ranks r
                                   join max_ranks mr on true),
     leaded_projects as (select u.github_user_id, count(distinct pl.project_id) as count
                         from project_leads pl
                                  join iam.users u on u.id = pl.user_id
                                  join projects p on p.id = pl.project_id and p.visibility = 'PUBLIC'
                         group by u.github_user_id)
select rank() over (order by 100 * r.contributions_rank +
                             2 * r.rewards_sent_rank +
                             1.5 * r.rewards_received_rank)
                             as rank,
       cume_dist() over (order by 100 * r.contributions_rank +
                                  2 * r.rewards_sent_rank +
                                  1.5 * r.rewards_received_rank)
                             as rank_percentile,
       r.*,
       coalesce(lp.count, 0) as leaded_project_count
from normalized_ranks r
         left join leaded_projects lp on lp.github_user_id = r.github_user_id
order by rank
;

CREATE UNIQUE INDEX global_users_ranks_pk ON global_users_ranks (github_user_id);

REFRESH MATERIALIZED VIEW global_users_ranks;
