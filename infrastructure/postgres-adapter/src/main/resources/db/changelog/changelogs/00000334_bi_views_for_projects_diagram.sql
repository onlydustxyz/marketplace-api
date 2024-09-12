CREATE FUNCTION accounting.usd_quote_at(currency_id UUID, at timestamp with time zone)
    RETURNS NUMERIC AS
$$
SELECT price
FROM accounting.historical_quotes hq
         JOIN currencies usd ON usd.id = hq.target_id and usd.code = 'USD'
WHERE hq.base_id = currency_id
  AND hq.timestamp <= at
ORDER BY hq.timestamp DESC
LIMIT 1
$$ LANGUAGE SQL;



CREATE VIEW bi_internal.exploded_rewards AS
SELECT r.id                      as reward_id,
       r.requested_at            as timestamp,
       r.recipient_id            as contributor_id,
       r.project_id              as project_id,
       rsd.amount_usd_equivalent as usd_amount,
       ppc.project_category_id   as project_category_id,
       lfe.language_id           as language_id,
       pe.ecosystem_id           as ecosystem_id,
       pp.program_id             as program_id
FROM rewards r
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         LEFT JOIN projects_ecosystems pe ON pe.project_id = r.project_id
         LEFT JOIN programs_projects pp ON pp.project_id = r.project_id
         LEFT JOIN projects_project_categories ppc ON ppc.project_id = r.project_id

         LEFT JOIN reward_items ri ON ri.reward_id = r.id
         LEFT JOIN completed_contributions c
                   ON ri.type = c.type::text::contribution_type
                       AND ri.number = c.github_number
                       AND r.recipient_id = c.contributor_id
         LEFT JOIN LATERAL ( SELECT DISTINCT lfe_1.language_id
                             FROM language_file_extensions lfe_1
                             WHERE lfe_1.extension = ANY (c.main_file_extensions)) lfe ON true
;


CREATE MATERIALIZED VIEW bi.reward_data AS
select r.reward_id                               as reward_id,
       r.timestamp                               as timestamp,
       r.contributor_id                          as contributor_id,
       r.project_id                              as project_id,
       r.usd_amount                              as usd_amount,
       array_agg(distinct r.language_id)         as language_ids,
       array_agg(distinct r.ecosystem_id)        as ecosystem_ids,
       array_agg(distinct r.program_id)          as program_ids,
       array_agg(distinct r.project_category_id) as project_category_ids
from bi_internal.exploded_rewards r
group by r.reward_id,
         r.timestamp,
         r.contributor_id,
         r.project_id,
         r.usd_amount;

create unique index bi_reward_data_pk on bi.reward_data (reward_id);
create index bi_reward_data_timestamp_program_ids_ecosystem_ids_idx on bi.reward_data (timestamp, program_ids, ecosystem_ids);



CREATE MATERIALIZED VIEW bi.daily_project_grants(project_id, program_id, day_timestamp, usd_amount) AS
select abt.project_id,
       abt.program_id,
       date_trunc('day', abt.timestamp) as day_timestamp,
       sum(abt.usd_amount)
from (SELECT abt.project_id,
             abt.program_id,
             CASE
                 WHEN abt.type = 'TRANSFER' THEN abt.amount * hq.usd_conversion_rate
                 ELSE abt.amount * hq.usd_conversion_rate * -1 END as usd_amount,
             abt.timestamp
      FROM accounting.account_book_transactions abt
               JOIN LATERAL (select accounting.usd_quote_at(abt.currency_id, abt.timestamp) as usd_conversion_rate) hq ON true
      WHERE abt.project_id IS NOT NULL
        AND (abt.type = 'TRANSFER' OR abt.type = 'REFUND')
        AND abt.reward_id IS NULL
        AND abt.payment_id IS NULL) abt
group by 1, 2, 3
;
create unique index bi_daily_project_grants_pk on bi.daily_project_grants (project_id, program_id, day_timestamp);
create unique index bi_daily_project_grants_pk_inv on bi.daily_project_grants (day_timestamp, program_id, project_id);



CREATE MATERIALIZED VIEW bi_internal.contribution_project_timestamps AS
SELECT DISTINCT c.created_at as timestamp,
                projects.id  as project_id
FROM completed_contributions c
         CROSS JOIN unnest(c.project_ids) AS projects(id)
;
create unique index bi_contribution_project_timestamps_pk on bi_internal.contribution_project_timestamps (project_id, timestamp);
create unique index bi_contribution_project_timestamps_pk_inv on bi_internal.contribution_project_timestamps (timestamp, project_id);



DROP MATERIALIZED VIEW bi.contribution_data;

CREATE MATERIALIZED VIEW bi.contribution_data AS
select *,

       (select max(previous.timestamp) as timestamp
        from bi_internal.contribution_project_timestamps previous
        where previous.project_id = any (c.project_ids)
          and previous.timestamp < c.timestamp) as previous_contribution_timestamp,

       (select min(next.timestamp) as timestamp
        from bi_internal.contribution_project_timestamps next
        where next.project_id = any (c.project_ids)
          and next.timestamp > c.timestamp)     as next_contribution_timestamp

from (select c.contribution_id                         as contribution_id,
             c.contributor_id                          as contributor_id,
             u.id                                      as contributor_user_id,
             kyc.country                               as contributor_country,
             c.timestamp                               as timestamp,
             c.is_first_contribution_on_onlydust       as is_first_contribution_on_onlydust,
             c.is_merged_pr                            as is_merged_pr,
             array_agg(distinct c.language_id)         as language_ids,
             array_agg(distinct c.ecosystem_id)        as ecosystem_ids,
             array_agg(distinct c.program_id)          as program_ids,
             array_agg(distinct c.project_id)          as project_ids,
             array_agg(distinct c.project_category_id) as project_category_ids
      from bi_internal.exploded_contributions c
               left join iam.users u on u.github_user_id = c.contributor_id
               left join accounting.billing_profiles_users bpu on bpu.user_id = u.id
               left join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id
      group by c.contribution_id,
               c.contributor_id,
               c.timestamp,
               c.is_first_contribution_on_onlydust,
               c.is_merged_pr,
               u.id,
               kyc.country) c;

create unique index bi_contribution_data_pk on bi.contribution_data (contribution_id);
create index bi_contribution_data_timestamp_idx on bi.contribution_data (timestamp);



CREATE MATERIALIZED VIEW bi.contribution_data_cross_projects AS
select c.contribution_id,
       projects.id as project_id,
       c.contributor_id,
       c.contributor_user_id,
       c.contributor_country,
       c.timestamp,
       c.is_first_contribution_on_onlydust,
       c.language_ids,
       c.ecosystem_ids,
       c.program_ids,
       c.project_category_ids,
       c.is_merged_pr,
       c.previous_contribution_timestamp,
       c.next_contribution_timestamp
from bi.contribution_data c
         CROSS JOIN unnest(c.project_ids) AS projects(id)
where projects.id is not null;

create unique index bi_cdcp_pk on bi.contribution_data_cross_projects (contribution_id, project_id);
create index bi_cdcp_timestamp_program_ids_ecosystem_ids_idx on bi.contribution_data_cross_projects (timestamp, program_ids, ecosystem_ids);
