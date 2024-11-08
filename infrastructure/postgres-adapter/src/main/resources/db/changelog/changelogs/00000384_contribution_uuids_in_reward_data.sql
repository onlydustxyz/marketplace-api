call drop_pseudo_projection('bi', 'reward_data');

call create_pseudo_projection('bi', 'reward_data', $$
select r.id                                                       as reward_id,
       r.requested_at                                             as timestamp,
       date_trunc('day', r.requested_at)                          as day_timestamp,
       date_trunc('week', r.requested_at)                         as week_timestamp,
       date_trunc('month', r.requested_at)                        as month_timestamp,
       date_trunc('quarter', r.requested_at)                      as quarter_timestamp,
       date_trunc('year', r.requested_at)                         as year_timestamp,
       r.recipient_id                                             as contributor_id,
       r.requestor_id                                             as requestor_id,
       r.project_id                                               as project_id,
       p.slug                                                     as project_slug,
       rsd.amount_usd_equivalent                                  as usd_amount,
       r.amount                                                   as amount,
       r.currency_id                                              as currency_id,
       array_agg(distinct pe.ecosystem_id)
       filter ( where pe.ecosystem_id is not null )               as ecosystem_ids,
       array_agg(distinct pp.program_id)
       filter ( where pp.program_id is not null )                 as program_ids,
       array_agg(distinct lfe.language_id)
       filter ( where lfe.language_id is not null )               as language_ids,
       array_agg(distinct ppc.project_category_id)
       filter ( where ppc.project_category_id is not null )       as project_category_ids,
       string_agg(currencies.name || ' ' || currencies.code, ' ') as search
from rewards r
         join accounting.reward_status_data rsd ON rsd.reward_id = r.id
         join projects p on p.id = r.project_id
         join currencies on currencies.id = r.currency_id
         left join projects_ecosystems pe on pe.project_id = r.project_id
         left join m_programs_projects pp on pp.project_id = r.project_id
         left join projects_project_categories ppc on ppc.project_id = r.project_id
         left join reward_items ri on r.id = ri.reward_id
         left join indexer_exp.contributions c on c.contributor_id = ri.recipient_id and
                                                  c.repo_id = ri.repo_id and
                                                  c.github_number = ri.number and
                                                  c.type::text::contribution_type = ri.type
         left join language_file_extensions lfe on lfe.extension = any (c.main_file_extensions)
group by r.id,
         r.requested_at,
         r.recipient_id,
         r.project_id,
         rsd.amount_usd_equivalent,
         r.amount,
         r.currency_id,
         p.slug
    $$, 'reward_id');

create index p_reward_data_contributor_id_project_id_index on bi.p_reward_data (contributor_id, project_id);
create unique index p_reward_data_contributor_id_reward_id_usd_amount_uindex on bi.p_reward_data (contributor_id, reward_id, usd_amount);

create index bi_p_reward_data_project_id_timestamp_idx on bi.p_reward_data (project_id, timestamp, currency_id);
create index bi_p_reward_data_project_id_day_timestamp_idx on bi.p_reward_data (project_id, day_timestamp, currency_id);
create index bi_p_reward_data_project_id_week_timestamp_idx on bi.p_reward_data (project_id, week_timestamp, currency_id);
create index bi_p_reward_data_project_id_month_timestamp_idx on bi.p_reward_data (project_id, month_timestamp, currency_id);
create index bi_p_reward_data_project_id_quarter_timestamp_idx on bi.p_reward_data (project_id, quarter_timestamp, currency_id);
create index bi_p_reward_data_project_id_year_timestamp_idx on bi.p_reward_data (project_id, year_timestamp, currency_id);
create index bi_p_reward_data_project_id_timestamp_idx_inv on bi.p_reward_data (timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_day_timestamp_idx_inv on bi.p_reward_data (day_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_week_timestamp_idx_inv on bi.p_reward_data (week_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_month_timestamp_idx_inv on bi.p_reward_data (month_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_quarter_timestamp_idx_inv on bi.p_reward_data (quarter_timestamp, project_id, currency_id);
create index bi_p_reward_data_project_id_year_timestamp_idx_inv on bi.p_reward_data (year_timestamp, project_id, currency_id);

create index bi_p_reward_data_contributor_id_timestamp_idx on bi.p_reward_data (contributor_id, timestamp, currency_id);
create index bi_p_reward_data_contributor_id_day_timestamp_idx on bi.p_reward_data (contributor_id, day_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_week_timestamp_idx on bi.p_reward_data (contributor_id, week_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_month_timestamp_idx on bi.p_reward_data (contributor_id, month_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_quarter_timestamp_idx on bi.p_reward_data (contributor_id, quarter_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_year_timestamp_idx on bi.p_reward_data (contributor_id, year_timestamp, currency_id);
create index bi_p_reward_data_contributor_id_timestamp_idx_inv on bi.p_reward_data (timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_day_timestamp_idx_inv on bi.p_reward_data (day_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_week_timestamp_idx_inv on bi.p_reward_data (week_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_month_timestamp_idx_inv on bi.p_reward_data (month_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_quarter_timestamp_idx_inv on bi.p_reward_data (quarter_timestamp, contributor_id, currency_id);
create index bi_p_reward_data_contributor_id_year_timestamp_idx_inv on bi.p_reward_data (year_timestamp, contributor_id, currency_id);
