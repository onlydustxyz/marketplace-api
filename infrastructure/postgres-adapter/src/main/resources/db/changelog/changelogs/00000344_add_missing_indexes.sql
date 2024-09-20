create index bi_contribution_data_project_id_day_timestamp_idx on bi.contribution_data (project_id, day_timestamp);
create index bi_contribution_data_project_id_week_timestamp_idx on bi.contribution_data (project_id, week_timestamp);
create index bi_contribution_data_project_id_month_timestamp_idx on bi.contribution_data (project_id, month_timestamp);
create index bi_contribution_data_project_id_quarter_timestamp_idx on bi.contribution_data (project_id, quarter_timestamp);
create index bi_contribution_data_project_id_year_timestamp_idx on bi.contribution_data (project_id, year_timestamp);

create index bi_contribution_data_project_id_day_timestamp_idx_inv on bi.contribution_data (day_timestamp, project_id);
create index bi_contribution_data_project_id_week_timestamp_idx_inv on bi.contribution_data (week_timestamp, project_id);
create index bi_contribution_data_project_id_month_timestamp_idx_inv on bi.contribution_data (month_timestamp, project_id);
create index bi_contribution_data_project_id_quarter_timestamp_idx_inv on bi.contribution_data (quarter_timestamp, project_id);
create index bi_contribution_data_project_id_year_timestamp_idx_inv on bi.contribution_data (year_timestamp, project_id);

create index bi_reward_data_project_id_day_timestamp_idx on bi.reward_data (project_id, day_timestamp, currency_id);
create index bi_reward_data_project_id_week_timestamp_idx on bi.reward_data (project_id, week_timestamp, currency_id);
create index bi_reward_data_project_id_month_timestamp_idx on bi.reward_data (project_id, month_timestamp, currency_id);
create index bi_reward_data_project_id_quarter_timestamp_idx on bi.reward_data (project_id, quarter_timestamp, currency_id);
create index bi_reward_data_project_id_year_timestamp_idx on bi.reward_data (project_id, year_timestamp, currency_id);

create index bi_project_grants_data_day_timestamp_idx_inv on bi.project_grants_data (project_id, day_timestamp);
create index bi_project_grants_data_week_timestamp_idx_inv on bi.project_grants_data (project_id, week_timestamp);
create index bi_project_grants_data_month_timestamp_idx_inv on bi.project_grants_data (project_id, month_timestamp);
create index bi_project_grants_data_quarter_timestamp_idx_inv on bi.project_grants_data (project_id, quarter_timestamp);
create index bi_project_grants_data_year_timestamp_idx_inv on bi.project_grants_data (project_id, year_timestamp);
