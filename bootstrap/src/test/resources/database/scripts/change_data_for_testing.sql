-- Needed to make migrations pass
create schema if not exists auth;
create table if not exists auth.users
(
    id uuid primary key
);
create table if not exists auth.user_providers
(
    id uuid primary key
);

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
