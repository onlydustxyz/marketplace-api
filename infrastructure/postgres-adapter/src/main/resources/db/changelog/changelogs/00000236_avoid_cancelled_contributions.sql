CREATE OR REPLACE VIEW public_contributions AS
select c.*,
       array_agg(distinct p.id) as project_ids
from indexer_exp.contributions c
         join indexer_exp.github_repos gr on gr.id = c.repo_id and gr.visibility = 'PUBLIC'
         join project_github_repos pgr on pgr.github_repo_id = gr.id
         join projects p on p.id = pgr.project_id and p.visibility = 'PUBLIC'
where c.status = 'COMPLETED'
group by c.id
;


REFRESH MATERIALIZED VIEW contributions_stats_per_user;
REFRESH MATERIALIZED VIEW contributions_stats_per_user_per_week;
REFRESH MATERIALIZED VIEW contributions_stats_per_ecosystem_per_user;
REFRESH MATERIALIZED VIEW contributions_stats_per_ecosystem_per_user_per_week;
REFRESH MATERIALIZED VIEW contributions_stats_per_language_per_user;
REFRESH MATERIALIZED VIEW received_rewards_stats_per_user;
REFRESH MATERIALIZED VIEW received_rewards_stats_per_user_per_week;
REFRESH MATERIALIZED VIEW received_rewards_stats_per_ecosystem_per_user;
REFRESH MATERIALIZED VIEW received_rewards_stats_per_ecosystem_per_user_per_week;
REFRESH MATERIALIZED VIEW received_rewards_stats_per_language_per_user;
REFRESH MATERIALIZED VIEW received_rewards_stats_per_project_per_user;
REFRESH MATERIALIZED VIEW global_users_ranks;
