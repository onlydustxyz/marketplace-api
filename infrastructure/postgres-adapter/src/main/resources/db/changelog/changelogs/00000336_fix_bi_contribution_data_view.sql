DROP MATERIALIZED VIEW bi.contribution_data_cross_projects;
DROP MATERIALIZED VIEW bi.contribution_data;


CREATE MATERIALIZED VIEW bi.contribution_data AS
select *,

       (select max(previous.timestamp) as timestamp
        from bi_internal.contribution_contributor_timestamps previous
        where previous.contributor_id = c.contributor_id
          and previous.timestamp < c.timestamp) as previous_contributor_contribution_timestamp,

       (select min(next.timestamp) as timestamp
        from bi_internal.contribution_contributor_timestamps next
        where next.contributor_id = c.contributor_id
          and next.timestamp > c.timestamp)     as next_contributor_contribution_timestamp

from (with registered_users as (select u.id             as id,
                                       u.github_user_id as github_user_id,
                                       kyc.country      as country
                                from iam.users u
                                         join accounting.billing_profiles_users bpu on bpu.user_id = u.id
                                         join accounting.kyc on kyc.billing_profile_id = bpu.billing_profile_id)
      select c.contribution_id                         as contribution_id,
             c.contributor_id                          as contributor_id,
             ru.id                                     as contributor_user_id,
             ru.country                                as contributor_country,
             c.timestamp                               as timestamp,
             c.is_first_contribution_on_onlydust       as is_first_contribution_on_onlydust,
             c.is_merged_pr                            as is_merged_pr,
             array_agg(distinct c.language_id)         as language_ids,
             array_agg(distinct c.ecosystem_id)        as ecosystem_ids,
             array_agg(distinct c.program_id)          as program_ids,
             array_agg(distinct c.project_id)          as project_ids,
             array_agg(distinct c.project_category_id) as project_category_ids
      from bi_internal.exploded_contributions c
               left join registered_users ru on ru.github_user_id = c.contributor_id
      group by c.contribution_id,
               c.contributor_id,
               c.timestamp,
               c.is_first_contribution_on_onlydust,
               c.is_merged_pr,
               ru.id,
               ru.country) c;

create unique index bi_contribution_data_pk on bi.contribution_data (contribution_id);
create index bi_contribution_data_timestamp_idx on bi.contribution_data (timestamp);



CREATE MATERIALIZED VIEW bi.contribution_data_cross_projects AS
select *,

       (select max(previous.timestamp) as timestamp
        from bi_internal.contribution_project_timestamps previous
        where previous.project_id = c.project_id
          and previous.timestamp < c.timestamp) as previous_project_contribution_timestamp,

       (select min(next.timestamp) as timestamp
        from bi_internal.contribution_project_timestamps next
        where next.project_id = c.project_id
          and next.timestamp > c.timestamp)     as next_project_contribution_timestamp

from (select c.contribution_id,
             projects.id as project_id,
             c.contributor_id,
             c.contributor_user_id,
             c.contributor_country,
             c.timestamp,
             c.language_ids,
             c.ecosystem_ids,
             c.program_ids,
             c.project_category_ids,
             c.is_merged_pr
      from bi.contribution_data c
               CROSS JOIN unnest(c.project_ids) AS projects(id)
      where projects.id is not null) c;

create unique index bi_cdcp_pk on bi.contribution_data_cross_projects (contribution_id, project_id);
create index bi_cdcp_timestamp_program_ids_ecosystem_ids_idx on bi.contribution_data_cross_projects (timestamp, program_ids, ecosystem_ids);
