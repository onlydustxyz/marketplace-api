create index if not exists bi_contribution_data_contributor_id_timestamp_idx_inv on bi.contribution_data (timestamp, contributor_id);
create index if not exists bi_contribution_data_contributor_id_day_timestamp_idx_inv on bi.contribution_data (day_timestamp, contributor_id);
create index if not exists bi_contribution_data_contributor_id_week_timestamp_idx_inv on bi.contribution_data (week_timestamp, contributor_id);
create index if not exists bi_contribution_data_contributor_id_month_timestamp_idx_inv on bi.contribution_data (month_timestamp, contributor_id);
create index if not exists bi_contribution_data_contributor_id_quarter_timestamp_idx_inv on bi.contribution_data (quarter_timestamp, contributor_id);
create index if not exists bi_contribution_data_contributor_id_year_timestamp_idx_inv on bi.contribution_data (year_timestamp, contributor_id);

create index if not exists bi_reward_data_contributor_id_day_timestamp_idx on bi.reward_data (contributor_id, timestamp, currency_id);
create index if not exists bi_reward_data_contributor_id_day_timestamp_idx on bi.reward_data (contributor_id, day_timestamp, currency_id);
create index if not exists bi_reward_data_contributor_id_week_timestamp_idx on bi.reward_data (contributor_id, week_timestamp, currency_id);
create index if not exists bi_reward_data_contributor_id_month_timestamp_idx on bi.reward_data (contributor_id, month_timestamp, currency_id);
create index if not exists bi_reward_data_contributor_id_quarter_timestamp_idx on bi.reward_data (contributor_id, quarter_timestamp, currency_id);
create index if not exists bi_reward_data_contributor_id_year_timestamp_idx on bi.reward_data (contributor_id, year_timestamp, currency_id);

create index if not exists bi_reward_data_contributor_id_day_timestamp_idx_inv on bi.reward_data (timestamp, contributor_id, currency_id);
create index if not exists bi_reward_data_contributor_id_day_timestamp_idx_inv on bi.reward_data (day_timestamp, contributor_id, currency_id);
create index if not exists bi_reward_data_contributor_id_week_timestamp_idx_inv on bi.reward_data (week_timestamp, contributor_id, currency_id);
create index if not exists bi_reward_data_contributor_id_month_timestamp_idx_inv on bi.reward_data (month_timestamp, contributor_id, currency_id);
create index if not exists bi_reward_data_contributor_id_quarter_timestamp_idx_inv on bi.reward_data (quarter_timestamp, contributor_id, currency_id);
create index if not exists bi_reward_data_contributor_id_year_timestamp_idx_inv on bi.reward_data (year_timestamp, contributor_id, currency_id);

