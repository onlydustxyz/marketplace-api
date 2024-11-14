create or replace function user_avatar_url(github_user_id bigint, fallback_url text) returns text
    stable
    language sql
as
$$
SELECT coalesce(
               (SELECT upi.avatar_url
                FROM user_profile_info upi
                         JOIN iam.users u ON u.id = upi.id
                WHERE u.github_user_id = $1),
               fallback_url
       )
$$;

create or replace function array_uniq_cat_agg(anyarray, anyarray) returns anyarray
    language sql
as
$$
SELECT ARRAY(SELECT DISTINCT unnest($1 || $2))
$$;

create or replace procedure create_pseudo_projection(IN schema text, IN name text, IN query text, IN pk_name text)
    language plpgsql
as
$$
DECLARE
    view_name              text;
    materialized_view_name text;
    projection_table_name  text;
BEGIN
    view_name := 'v_' || name;
    materialized_view_name := 'm_' || name;
    projection_table_name := 'p_' || name;

    EXECUTE format('CREATE VIEW %I.%I AS SELECT v.*, md5(v::text) as hash from (%s) v', schema, view_name, query);

    EXECUTE format('CREATE MATERIALIZED VIEW %I.%I AS SELECT * FROM %I.%I', schema, materialized_view_name, schema, view_name);
    EXECUTE format('CREATE UNIQUE INDEX %I_%I_pk ON %I.%I (%I)', schema, 'm_' || name, schema, materialized_view_name, pk_name);
    EXECUTE format('CREATE UNIQUE INDEX %I_%I_%I_hash_idx ON %I.%I (%I, hash)', schema, 'm_' || name, pk_name, schema, materialized_view_name, pk_name);

    EXECUTE format('CREATE TABLE %I.%I AS TABLE %I.%I', schema, projection_table_name, schema, materialized_view_name);
    EXECUTE format('ALTER TABLE %I.%I ADD PRIMARY KEY (%I)', schema, projection_table_name, pk_name);
    EXECUTE format('CREATE UNIQUE INDEX %I_%I_%I_hash_idx ON %I.%I (%I, hash)', schema, 'p_' || name, pk_name, schema, projection_table_name, pk_name);

    EXECUTE format('ALTER TABLE %I.%I ADD COLUMN tech_created_at timestamptz NOT NULL DEFAULT now()', schema, projection_table_name);
    EXECUTE format('ALTER TABLE %I.%I ADD COLUMN tech_updated_at timestamptz NOT NULL DEFAULT now()', schema, projection_table_name);
    EXECUTE format('CREATE TRIGGER %s_%s_set_tech_updated_at ' ||
                   'BEFORE UPDATE ON %I.%I ' ||
                   'FOR EACH ROW EXECUTE FUNCTION set_tech_updated_at()', schema, name, schema, projection_table_name);
END
$$;

create or replace procedure drop_pseudo_projection(IN schema text, IN name text)
    language plpgsql
as
$$
DECLARE
    view_name              text;
    materialized_view_name text;
    projection_table_name  text;
BEGIN
    view_name := 'v_' || name;
    materialized_view_name := 'm_' || name;
    projection_table_name := 'p_' || name;

    EXECUTE format('DROP TABLE %I.%I', schema, projection_table_name);
    EXECUTE format('DROP MATERIALIZED VIEW %I.%I', schema, materialized_view_name);
    EXECUTE format('DROP VIEW %I.%I', schema, view_name);
END
$$;

create or replace procedure refresh_pseudo_projection(IN schema text, IN name text, IN pk_name text)
    language plpgsql
as
$$
DECLARE
    materialized_view_name text;
    projection_table_name  text;
BEGIN
    materialized_view_name := 'm_' || name;
    projection_table_name := 'p_' || name;

    EXECUTE format('refresh materialized view %I.%I', schema, materialized_view_name);

    -- NOTE: to avoid deadlocks, rows are properly locked before being deleted.
    -- If a row is already locked, IT WILL BE SKIPPED as it is already being updated by another refresh.
    EXECUTE format(
            'delete from %I.%I where %I in ( select %I from %I.%I where not exists(select 1 from %I.%I m where m.%I = %I.%I and m.hash = %I.hash) for update skip locked )',
            schema, projection_table_name, pk_name,
            pk_name, schema, projection_table_name,
            schema, materialized_view_name, pk_name, projection_table_name, pk_name, projection_table_name);

    EXECUTE format('insert into %I.%I select * from %I.%I m where not exists(select 1 from %I.%I p where p.%I = m.%I) order by %I on conflict (%I) do nothing',
                   schema, projection_table_name, schema, materialized_view_name, schema, projection_table_name, pk_name, pk_name, pk_name, pk_name);
END
$$;

create or replace procedure refresh_pseudo_projection(IN schema text, IN name text, IN pk_name text, IN params jsonb)
    language plpgsql
as
$$
DECLARE
    condition text;
    key       text;
    value     text;
BEGIN
    condition := ' true ';
    FOR key, value IN SELECT * FROM jsonb_each_text(params)
        LOOP
            condition := condition || format(' and %I = %L', key, value);
        END LOOP;

    CALL refresh_pseudo_projection_unsafe(schema, name, pk_name, condition);
END
$$;

create or replace procedure refresh_pseudo_projection_unsafe(IN schema text, IN name text, IN pk_name text, IN condition text)
    language plpgsql
as
$$
DECLARE
    view_name             text;
    projection_table_name text;
BEGIN
    view_name := 'v_' || name;
    projection_table_name := 'p_' || name;

    -- NOTE: to avoid deadlocks, rows are properly locked before being deleted.
    -- If a row is already locked, IT WILL BE SKIPPED, because we do not want this job to wait for the global-refresh to be done. We need it to be fast.
    EXECUTE format('delete from %I.%I where %I in ( select %I from %I.%I where %s for update skip locked )',
                   schema, projection_table_name, pk_name,
                   pk_name, schema, projection_table_name, condition);

    EXECUTE format('with to_insert as materialized (select * from %I.%I v where %s)' ||
                   'insert into %I.%I select * from to_insert where not exists(select 1 from %I.%I p where p.%I = to_insert.%I) order by %I on conflict (%I) do nothing',
                   schema, view_name, condition,
                   schema, projection_table_name, schema, projection_table_name, pk_name, pk_name, pk_name, pk_name);
END
$$;

create table if not exists reward_items
(
    reward_id         uuid                    not null
        references rewards,
    number            bigint                  not null,
    repo_id           bigint                  not null,
    id                text                    not null,
    type              contribution_type       not null,
    project_id        uuid                    not null
        references projects,
    recipient_id      bigint                  not null,
    tech_created_at   timestamptz default now() not null,
    tech_updated_at   timestamptz default now() not null,
    contribution_uuid uuid                    not null,
    primary key (reward_id, repo_id, number)
);

create or replace function get_reward_ids_of_contributions(contributionuuids uuid[]) returns uuid[]
    stable
    parallel safe
    language sql
as
$$
select array_agg(distinct ri.reward_id)
from reward_items ri
where ri.contribution_uuid = any (contributionUuids)
$$;

create or replace function get_contribution_uuids_of_reward(rewardid uuid) returns uuid[]
    stable
    parallel safe
    language sql
as
$$
select array_agg(distinct ri.contribution_uuid)
from reward_items ri
where ri.reward_id = rewardId
$$;

create aggregate jsonb_concat_agg(jsonb) (
    sfunc = jsonb_concat,
    stype = jsonb,
    initcond = '{}'
    );

create aggregate array_uniq_cat_agg(anyarray) (
    sfunc = array_uniq_cat_agg,
    stype = anyarray,
    initcond = '{}'
    );


create table if not exists erc20
(
    blockchain      accounting.network      not null,
    address         text                    not null,
    name            text                    not null,
    symbol          text                    not null,
    decimals        integer                 not null,
    total_supply    numeric                 not null,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    currency_id     uuid                    not null
        references currencies,
    primary key (blockchain, address)
);


create table if not exists applications
(
    id              uuid                                                         not null
        primary key,
    received_at     timestamptz                                                    not null,
    project_id      uuid                                                         not null
        references projects,
    issue_id        bigint,
    comment_id      bigint,
    comment_body    text,
    tech_created_at timestamptz          default now()                             not null,
    tech_updated_at timestamptz          default now()                             not null,
    origin          application_origin default 'MARKETPLACE'::application_origin not null,
    applicant_id    bigint                                                       not null
        constraint applications_applicant_id_fkey1
            references indexer_exp.github_accounts,
    ignored_at      timestamptz,
    constraint applications_project_id_applicant_id_issue_id_unique
        unique (project_id, applicant_id, issue_id)
);

create index if not exists reward_items_id_index
    on reward_items (id);

create unique index if not exists reward_items_contribution_uuid_index
    on reward_items (contribution_uuid, reward_id);

create unique index if not exists reward_items_contribution_uuid_index_inv
    on reward_items (reward_id, contribution_uuid);

create table if not exists sponsor_leads
(
    sponsor_id      uuid                    not null
        constraint sponsors_users_sponsor_id_fkey
            references sponsors,
    user_id         uuid                    not null
        constraint sponsors_users_user_id_fkey
            references iam.users,
    tech_created_at timestamptz default now() not null,
    constraint sponsors_users_pkey
        primary key (user_id, sponsor_id)
);

create table if not exists hackathon_registrations
(
    hackathon_id    uuid                    not null
        references hackathons,
    user_id         uuid                    not null
        references iam.users,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    primary key (hackathon_id, user_id)
);

create table if not exists committee_juries
(
    user_id         uuid                    not null
        references iam.users,
    committee_id    uuid                    not null
        references committees,
    tech_created_at timestamptz default now() not null,
    primary key (user_id, committee_id)
);

create table if not exists committee_jury_votes
(
    committee_id    uuid                    not null
        references committees,
    criteria_id     uuid                    not null
        references committee_jury_criteria,
    project_id      uuid                    not null
        references projects,
    user_id         uuid                    not null
        references iam.users,
    score           integer,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    primary key (committee_id, criteria_id, project_id, user_id)
);

create table if not exists banners_closed_by
(
    banner_id       uuid                    not null
        references banners,
    user_id         uuid                    not null
        references iam.users,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    primary key (banner_id, user_id)
);

create table if not exists program_leads
(
    program_id      uuid                    not null
        references programs,
    user_id         uuid                    not null
        references iam.users,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    primary key (program_id, user_id)
);

create table if not exists ecosystem_leads
(
    ecosystem_id    uuid                                   not null
        references ecosystems,
    user_id         uuid                                   not null
        references iam.users,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    primary key (ecosystem_id, user_id)
);

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

create materialized view if not exists contributions_stats_per_user as
SELECT c.contributor_id,
       count(DISTINCT c.id)                                                                       AS contribution_count,
       array_agg(DISTINCT unnested.project_ids)                                                   AS project_ids,
       array_length(array_agg(DISTINCT unnested.project_ids), 1)                                  AS project_count,
       count(DISTINCT date_trunc('week'::text, c.created_at))                                     AS contributed_week_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'CODE_REVIEW'::indexer_exp.contribution_type)  AS code_review_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'ISSUE'::indexer_exp.contribution_type)        AS issue_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'PULL_REQUEST'::indexer_exp.contribution_type) AS pull_request_count
FROM public_contributions c
         CROSS JOIN LATERAL unnest(c.project_ids) unnested(project_ids)
GROUP BY c.contributor_id;

create unique index if not exists contributions_stats_per_user_pk
    on contributions_stats_per_user (contributor_id);

create materialized view if not exists contributions_stats_per_user_per_week as
SELECT c.contributor_id,
       date_trunc('week'::text, c.created_at)                                                     AS created_at_week,
       count(DISTINCT c.id)                                                                       AS contribution_count,
       array_agg(DISTINCT unnested.project_ids)                                                   AS project_ids,
       array_length(array_agg(DISTINCT unnested.project_ids), 1)                                  AS project_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'CODE_REVIEW'::indexer_exp.contribution_type)  AS code_review_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'ISSUE'::indexer_exp.contribution_type)        AS issue_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'PULL_REQUEST'::indexer_exp.contribution_type) AS pull_request_count
FROM public_contributions c
         CROSS JOIN LATERAL unnest(c.project_ids) unnested(project_ids)
GROUP BY c.contributor_id, (date_trunc('week'::text, c.created_at));

create unique index if not exists contributions_stats_per_user_per_week_pk
    on contributions_stats_per_user_per_week (contributor_id, created_at_week);

create unique index if not exists contributions_stats_per_user_per_week_rpk
    on contributions_stats_per_user_per_week (created_at_week, contributor_id);

create materialized view if not exists contributions_stats_per_ecosystem_per_user as
SELECT c.contributor_id,
       pe.ecosystem_id,
       count(DISTINCT c.id)                                                                       AS contribution_count,
       array_agg(DISTINCT pe.project_id)                                                          AS project_ids,
       array_length(array_agg(DISTINCT pe.project_id), 1)                                         AS project_count,
       count(DISTINCT date_trunc('week'::text, c.created_at))                                     AS contributed_week_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'CODE_REVIEW'::indexer_exp.contribution_type)  AS code_review_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'ISSUE'::indexer_exp.contribution_type)        AS issue_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'PULL_REQUEST'::indexer_exp.contribution_type) AS pull_request_count
FROM public_contributions c
         JOIN projects_ecosystems pe ON pe.project_id = ANY (c.project_ids)
GROUP BY pe.ecosystem_id, c.contributor_id;

create unique index if not exists contributions_stats_per_ecosystem_per_user_pk
    on contributions_stats_per_ecosystem_per_user (ecosystem_id, contributor_id);

create unique index if not exists contributions_stats_per_ecosystem_per_user_rpk
    on contributions_stats_per_ecosystem_per_user (contributor_id, ecosystem_id);

create materialized view if not exists contributions_stats_per_ecosystem_per_user_per_week as
SELECT c.contributor_id,
       pe.ecosystem_id,
       date_trunc('week'::text, c.created_at)                                                     AS created_at_week,
       count(DISTINCT c.id)                                                                       AS contribution_count,
       array_agg(DISTINCT pe.project_id)                                                          AS project_ids,
       array_length(array_agg(DISTINCT pe.project_id), 1)                                         AS project_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'CODE_REVIEW'::indexer_exp.contribution_type)  AS code_review_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'ISSUE'::indexer_exp.contribution_type)        AS issue_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'PULL_REQUEST'::indexer_exp.contribution_type) AS pull_request_count
FROM public_contributions c
         JOIN projects_ecosystems pe ON pe.project_id = ANY (c.project_ids)
GROUP BY pe.ecosystem_id, c.contributor_id, (date_trunc('week'::text, c.created_at));

create unique index if not exists contributions_stats_per_ecosystem_per_user_per_week_pk
    on contributions_stats_per_ecosystem_per_user_per_week (ecosystem_id, contributor_id, created_at_week);

create unique index if not exists contributions_stats_per_ecosystem_per_user_per_week_rpk
    on contributions_stats_per_ecosystem_per_user_per_week (contributor_id, ecosystem_id, created_at_week);

CREATE OR REPLACE VIEW public_received_rewards AS
select r.*,
       rsd.amount_usd_equivalent,
       rsd.usd_conversion_rate,
       rsd.invoice_received_at,
       rsd.paid_at,
       rsd.networks,
       coalesce(array_agg(distinct unnested.main_file_extensions)
                filter (where unnested.main_file_extensions is not null), '{}') as main_file_extensions
from rewards r
         join reward_items ri on ri.reward_id = r.id
         join projects p on p.id = r.project_id and p.visibility = 'PUBLIC'
         join accounting.reward_status_data rsd ON rsd.reward_id = r.id
         left join indexer_exp.github_pull_requests gpr
                   on gpr.id = (case when ri.type = 'PULL_REQUEST' then cast(ri.id as bigint) else null end)
         left join unnest(gpr.main_file_extensions) unnested(main_file_extensions) on true
group by r.id, rsd.reward_id;

create materialized view if not exists received_rewards_stats_per_user_per_week as
SELECT r.recipient_id,
       date_trunc('week'::text, r.requested_at) AS requested_at_week,
       count(DISTINCT r.id)                     AS reward_count,
       count(DISTINCT r.project_id)             AS project_count,
       round(sum(r.amount_usd_equivalent), 2)   AS usd_total,
       array_agg(DISTINCT r.project_id)         AS project_ids
FROM public_received_rewards r
GROUP BY r.recipient_id, (date_trunc('week'::text, r.requested_at));

create unique index if not exists received_rewards_stats_per_user_per_week_pk
    on received_rewards_stats_per_user_per_week (recipient_id, requested_at_week);

create unique index if not exists received_rewards_stats_per_user_per_week_rpk
    on received_rewards_stats_per_user_per_week (requested_at_week, recipient_id);

create materialized view if not exists received_rewards_stats_per_ecosystem_per_user_per_week as
SELECT r.recipient_id,
       pe.ecosystem_id,
       date_trunc('week'::text, r.requested_at) AS requested_at_week,
       count(DISTINCT r.id)                     AS reward_count,
       count(DISTINCT r.project_id)             AS project_count,
       round(sum(r.amount_usd_equivalent), 2)   AS usd_total,
       array_agg(DISTINCT r.project_id)         AS project_ids
FROM public_received_rewards r
         JOIN projects_ecosystems pe ON pe.project_id = r.project_id
GROUP BY pe.ecosystem_id, r.recipient_id, (date_trunc('week'::text, r.requested_at));

create unique index if not exists received_rewards_stats_per_ecosystem_per_user_per_week_pk
    on received_rewards_stats_per_ecosystem_per_user_per_week (ecosystem_id, recipient_id, requested_at_week);

create unique index if not exists received_rewards_stats_per_ecosystem_per_user_per_week_rpk
    on received_rewards_stats_per_ecosystem_per_user_per_week (recipient_id, ecosystem_id, requested_at_week);

create materialized view if not exists project_languages as
SELECT DISTINCT pgr.project_id,
                lfe.language_id
FROM public_contributions c
         JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id
         JOIN language_file_extensions lfe ON lfe.extension = ANY (c.main_file_extensions);

create unique index if not exists project_languages_project_id_language_id_uindex
    on project_languages (project_id, language_id);

create materialized view if not exists received_rewards_stats_per_user as
SELECT r.recipient_id,
       count(DISTINCT r.id)                                                                        AS reward_count,
       count(DISTINCT r.project_id)                                                                AS project_count,
       round(sum(r.amount_usd_equivalent), 2)                                                      AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at))                                   AS rewarded_month_count,
       array_agg(DISTINCT r.project_id)                                                            AS project_ids,
       round(max(r.amount_usd_equivalent), 2)                                                      AS max_usd,
       count(DISTINCT r.id)
       FILTER (WHERE rs.status = 'PENDING_REQUEST'::accounting.reward_status)                      AS pending_request_reward_count
FROM public_received_rewards r
         JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
GROUP BY r.recipient_id;

create unique index if not exists received_rewards_stats_per_user_pk
    on received_rewards_stats_per_user (recipient_id);

create materialized view if not exists received_rewards_stats_per_ecosystem_per_user as
SELECT r.recipient_id,
       pe.ecosystem_id,
       count(DISTINCT r.id)                                                                        AS reward_count,
       count(DISTINCT r.project_id)                                                                AS project_count,
       round(sum(r.amount_usd_equivalent), 2)                                                      AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at))                                   AS rewarded_month_count,
       array_agg(DISTINCT r.project_id)                                                            AS project_ids,
       round(max(r.amount_usd_equivalent), 2)                                                      AS max_usd,
       count(DISTINCT r.id)
       FILTER (WHERE rs.status = 'PENDING_REQUEST'::accounting.reward_status)                      AS pending_request_reward_count
FROM public_received_rewards r
         JOIN projects_ecosystems pe ON pe.project_id = r.project_id
         JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
GROUP BY pe.ecosystem_id, r.recipient_id;

create unique index if not exists received_rewards_stats_per_ecosystem_per_user_pk
    on received_rewards_stats_per_ecosystem_per_user (ecosystem_id, recipient_id);

create unique index if not exists received_rewards_stats_per_ecosystem_per_user_rpk
    on received_rewards_stats_per_ecosystem_per_user (recipient_id, ecosystem_id);


CREATE VIEW users_rank_per_contribution as
WITH ranks AS (SELECT u.github_user_id                                                AS github_user_id,
                      rank() OVER (ORDER BY s.contribution_count DESC NULLS LAST)     AS contribution_count,
                      rank() OVER (ORDER BY s.contributed_week_count DESC NULLS LAST) AS contributed_week_count,
                      rank() OVER (ORDER BY s.project_count DESC NULLS LAST)          AS project_count
               FROM iam.all_users u
                        LEFT JOIN contributions_stats_per_user s ON s.contributor_id = u.github_user_id)
SELECT r.github_user_id                                                                                    AS github_user_id,
       rank() over (order by 2 * r.contribution_count + r.contributed_week_count + 0.05 * r.project_count) AS rank,
       stats.contribution_count                                                                            AS contributions_count,
       stats.contributed_week_count                                                                        AS contributed_week_count,
       stats.project_count                                                                                 AS projects_count,
       stats.project_ids                                                                                   AS project_ids
FROM ranks r
         LEFT JOIN contributions_stats_per_user stats ON stats.contributor_id = r.github_user_id
         JOIN indexer_exp.github_accounts ga on ga.id = r.github_user_id and ga.type = 'USER'
;

CREATE VIEW users_rank_per_reward_received as
WITH ranks AS (SELECT u.github_user_id                                               AS github_user_id,
                      rank() OVER (ORDER BY rr.reward_count DESC NULLS LAST)         AS reward_count,
                      rank() OVER (ORDER BY rr.usd_total DESC NULLS LAST)            AS usd_total,
                      rank() OVER (ORDER BY rr.rewarded_month_count DESC NULLS LAST) AS rewarded_month_count,
                      rank() OVER (ORDER BY rr.project_count DESC NULLS LAST)        AS project_count
               FROM iam.all_users u
                        LEFT JOIN received_rewards_stats_per_user rr ON rr.recipient_id = u.github_user_id)
SELECT r.github_user_id                                                                                       AS github_user_id,
       rank()
       over (order by 5 * r.usd_total + 2 * r.rewarded_month_count + r.reward_count + 0.05 * r.project_count) AS rank,
       stats.usd_total                                                                                        AS usd_total,
       stats.rewarded_month_count                                                                             AS rewarded_month_count,
       stats.reward_count                                                                                     AS reward_count,
       stats.project_count                                                                                    AS project_count
FROM ranks r
         JOIN received_rewards_stats_per_user stats ON stats.recipient_id = r.github_user_id
         JOIN indexer_exp.github_accounts ga ON ga.id = r.github_user_id AND ga.type = 'USER'
ORDER BY rank;

create view users_rank_per_reward_sent as
WITH rewards_stats_per_requestor AS (SELECT r.requestor_id,
                                            count(DISTINCT r.project_id)                                     AS project_count,
                                            count(DISTINCT r.recipient_id)                                   AS rewarded_user_count,
                                            round(sum(rsd.amount_usd_equivalent), 2)                         AS usd_total,
                                            count(DISTINCT date_trunc('month'::text, r.requested_at))
                                            FILTER ( WHERE date_trunc('month', r.requested_at) >
                                                           date_trunc('month', now() - interval '6 month') ) AS rewarded_month_count
                                     FROM rewards r
                                              JOIN accounting.reward_status_data rsd ON r.id = rsd.reward_id
                                     GROUP BY r.requestor_id),
     ranks AS (SELECT u.user_id                                                      AS user_id,
                      u.github_user_id                                               AS github_user_id,
                      rank() OVER (ORDER BY rr.usd_total DESC NULLS LAST)            AS usd_total,
                      rank() OVER (ORDER BY rr.rewarded_user_count DESC NULLS LAST)  AS rewarded_user_count,
                      rank() OVER (ORDER BY rr.rewarded_month_count DESC NULLS LAST) AS rewarded_month_count,
                      rank() OVER (ORDER BY rr.project_count DESC NULLS LAST)        AS project_count
               FROM iam.all_users u
                        LEFT JOIN rewards_stats_per_requestor rr ON rr.requestor_id = u.user_id)
SELECT r.github_user_id                              AS github_user_id,
       rank() over (order by 5 * r.usd_total + 3 * r.rewarded_user_count + 2 * r.rewarded_month_count +
                             0.05 * r.project_count) AS rank,
       stats.usd_total                               AS usd_total,
       stats.rewarded_user_count                     AS rewarded_user_count,
       stats.rewarded_month_count                    AS rewarded_month_count,
       stats.project_count                           AS project_count
FROM ranks r
         JOIN rewards_stats_per_requestor stats ON stats.requestor_id = r.user_id
ORDER BY rank;

create materialized view if not exists global_users_ranks as
WITH ranks AS (SELECT COALESCE(c.github_user_id, rr.github_user_id, rs.github_user_id) AS github_user_id,
                      rank() OVER (ORDER BY c.rank)                                    AS contributions_rank,
                      rank() OVER (ORDER BY rs.rank)                                   AS rewards_sent_rank,
                      rank() OVER (ORDER BY rr.rank)                                   AS rewards_received_rank,
                      c.contributions_count                                            AS contribution_count,
                      c.projects_count                                                 AS contributed_project_count,
                      rr.reward_count
               FROM users_rank_per_contribution c
                        FULL JOIN users_rank_per_reward_received rr ON rr.github_user_id = c.github_user_id
                        FULL JOIN users_rank_per_reward_sent rs ON rs.github_user_id = c.github_user_id),
     max_ranks AS (SELECT max(ranks.contributions_rank)    AS contributions_rank,
                          max(ranks.rewards_sent_rank)     AS rewards_sent_rank,
                          max(ranks.rewards_received_rank) AS rewards_received_rank
                   FROM ranks),
     normalized_ranks AS (SELECT r_1.github_user_id,
                                 round(100.0 * r_1.contributions_rank::numeric / mr.contributions_rank::numeric,
                                       2)                                                                         AS contributions_rank,
                                 round(100.0 * r_1.rewards_sent_rank::numeric / mr.rewards_sent_rank::numeric,
                                       2)                                                                         AS rewards_sent_rank,
                                 round(100.0 * r_1.rewards_received_rank::numeric / mr.rewards_received_rank::numeric,
                                       2)                                                                         AS rewards_received_rank,
                                 r_1.contribution_count,
                                 r_1.contributed_project_count,
                                 r_1.reward_count
                          FROM ranks r_1
                                   JOIN max_ranks mr ON true),
     leaded_projects AS (SELECT u.github_user_id,
                                count(DISTINCT pl.project_id) AS count
                         FROM project_leads pl
                                  JOIN iam.users u ON u.id = pl.user_id
                                  JOIN projects p ON p.id = pl.project_id AND p.visibility = 'PUBLIC'::project_visibility
                         GROUP BY u.github_user_id)
SELECT rank() OVER (ORDER BY (100::numeric * r.contributions_rank + 2::numeric * r.rewards_sent_rank +
                              1.5 * r.rewards_received_rank))      AS rank,
       cume_dist() OVER (ORDER BY (100::numeric * r.contributions_rank + 2::numeric * r.rewards_sent_rank +
                                   1.5 * r.rewards_received_rank)) AS rank_percentile,
       r.github_user_id,
       r.contributions_rank,
       r.rewards_sent_rank,
       r.rewards_received_rank,
       r.contribution_count,
       r.contributed_project_count,
       r.reward_count,
       COALESCE(lp.count, 0::bigint)                               AS leaded_project_count
FROM normalized_ranks r
         LEFT JOIN leaded_projects lp ON lp.github_user_id = r.github_user_id
ORDER BY (rank() OVER (ORDER BY (100::numeric * r.contributions_rank + 2::numeric * r.rewards_sent_rank +
                                 1.5 * r.rewards_received_rank)));

create unique index if not exists global_users_ranks_pk
    on global_users_ranks (github_user_id);

create materialized view if not exists repo_languages as
SELECT DISTINCT c.repo_id,
                lfe.language_id,
                count(c.id)                                                                 AS contribution_count,
                rank() OVER (PARTITION BY c.repo_id ORDER BY (count(c.id)) DESC NULLS LAST) AS rank
FROM indexer_exp.contributions c
         JOIN language_file_extensions lfe ON lfe.extension = ANY (c.main_file_extensions)
GROUP BY c.repo_id, lfe.language_id;

create unique index if not exists repo_languages_pk
    on repo_languages (repo_id, language_id);

create materialized view if not exists received_rewards_stats_per_language_per_user as
SELECT r.recipient_id,
       lfe.language_id,
       count(DISTINCT r.id)                                                                        AS reward_count,
       count(DISTINCT r.project_id)                                                                AS project_count,
       round(sum(r.amount_usd_equivalent), 2)                                                      AS usd_total,
       count(DISTINCT date_trunc('month'::text, r.requested_at))                                   AS rewarded_month_count,
       array_agg(DISTINCT r.project_id)                                                            AS project_ids,
       round(max(r.amount_usd_equivalent), 2)                                                      AS max_usd,
       count(DISTINCT r.id)
       FILTER (WHERE rs.status = 'PENDING_REQUEST'::accounting.reward_status)                      AS pending_request_reward_count
FROM public_received_rewards r
         JOIN LATERAL ( SELECT DISTINCT lfe_1.language_id
                        FROM language_file_extensions lfe_1
                        WHERE lfe_1.extension = ANY (r.main_file_extensions)) lfe ON true
         JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
GROUP BY lfe.language_id, r.recipient_id;

create unique index if not exists received_rewards_stats_per_language_per_user_pk
    on received_rewards_stats_per_language_per_user (language_id, recipient_id);

create unique index if not exists received_rewards_stats_per_language_per_user_rpk
    on received_rewards_stats_per_language_per_user (recipient_id, language_id);

create materialized view if not exists contributions_stats_per_language_per_user as
SELECT c.contributor_id,
       lfe.language_id,
       count(DISTINCT c.id)                                                                       AS contribution_count,
       array_agg(DISTINCT unnested.project_ids)                                                   AS project_ids,
       array_length(array_agg(DISTINCT unnested.project_ids), 1)                                  AS project_count,
       count(DISTINCT date_trunc('week'::text, c.created_at))                                     AS contributed_week_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'CODE_REVIEW'::indexer_exp.contribution_type)  AS code_review_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'ISSUE'::indexer_exp.contribution_type)        AS issue_count,
       count(DISTINCT c.id) FILTER (WHERE c.type = 'PULL_REQUEST'::indexer_exp.contribution_type) AS pull_request_count
FROM public_contributions c
         CROSS JOIN LATERAL unnest(c.project_ids) unnested(project_ids)
         JOIN LATERAL ( SELECT DISTINCT lfe_1.language_id
                        FROM language_file_extensions lfe_1
                        WHERE lfe_1.extension = ANY (c.main_file_extensions)) lfe ON true
GROUP BY lfe.language_id, c.contributor_id;

create unique index if not exists contributions_stats_per_language_per_user_pk
    on contributions_stats_per_language_per_user (language_id, contributor_id);

create unique index if not exists contributions_stats_per_language_per_user_rpk
    on contributions_stats_per_language_per_user (contributor_id, language_id);

create or replace view v_programs_projects(program_id, project_id, has_remaining_grants) as
WITH allocations AS (SELECT abt.currency_id,
                            abt.program_id,
                            abt.project_id,
                            COALESCE(sum(abt.amount)
                                     FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND
                                                   abt.reward_id IS NULL), 0::numeric) - COALESCE(sum(abt.amount)
                                                                                                  FILTER (WHERE
                                                                                                      abt.type =
                                                                                                      'REFUND'::accounting.transaction_type AND
                                                                                                      abt.reward_id IS NULL),
                                                                                                  0::numeric) -
                            COALESCE(sum(abt.amount)
                                     FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND
                                                   abt.reward_id IS NOT NULL), 0::numeric) + COALESCE(sum(abt.amount)
                                                                                                      FILTER (WHERE
                                                                                                          abt.type =
                                                                                                          'REFUND'::accounting.transaction_type AND
                                                                                                          abt.reward_id IS NOT NULL),
                                                                                                      0::numeric) AS remaining_amount
                     FROM accounting.all_transactions abt
                     WHERE abt.project_id IS NOT NULL
                       AND abt.payment_id IS NULL
                     GROUP BY abt.currency_id, abt.program_id, abt.project_id)
SELECT allocations.program_id,
       allocations.project_id,
       bool_or(allocations.remaining_amount > 0::numeric) AS has_remaining_grants
FROM allocations
GROUP BY allocations.program_id, allocations.project_id;

create materialized view if not exists m_active_programs_projects as
SELECT v_programs_projects.program_id,
       v_programs_projects.project_id
FROM v_programs_projects
WHERE v_programs_projects.has_remaining_grants IS TRUE;

create unique index if not exists m_active_programs_projects_pk
    on m_active_programs_projects (program_id, project_id);

create unique index if not exists m_active_programs_projects_pk_inv
    on m_active_programs_projects (project_id, program_id);

CREATE OR REPLACE VIEW projects_pending_contributors AS
SELECT pgr.project_id                       AS project_id,
       rc.contributor_id                    AS github_user_id,
       sum(rc.completed_contribution_count) AS completed_contribution_count,
       sum(rc.total_contribution_count)     AS total_contribution_count
FROM indexer_exp.repos_contributors rc
         JOIN public.project_github_repos pgr on pgr.github_repo_id = rc.repo_id
         JOIN indexer_exp.github_repos gr on gr.id = pgr.github_repo_id and gr.visibility = 'PUBLIC'
GROUP BY pgr.project_id, rc.contributor_id;

CREATE VIEW projects_contributors AS
SELECT *
FROM projects_pending_contributors
WHERE completed_contribution_count > 0;

create view projects_good_first_issues as
SELECT DISTINCT pgr.project_id as project_id,
                i.id           as issue_id
FROM project_github_repos pgr
         join indexer_exp.github_issues i on i.repo_id = pgr.github_repo_id
         LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
         JOIN indexer_exp.github_issues_labels gil ON i.id = gil.issue_id
         JOIN indexer_exp.github_labels gl on gil.label_id = gl.id AND gl.name ilike '%good%first%issue%'
WHERE i.status = 'OPEN'
  AND gia.user_id IS NULL
;

create or replace view v_user_project_recommendations(pk, project_id, github_user_id, user_id, rank, hash) as
SELECT v.pk,
       v.project_id,
       v.github_user_id,
       v.user_id,
       v.rank,
       md5(v.*::text) AS hash
FROM (WITH project_recommendations AS (WITH user_languages AS (SELECT upi.id                             AS user_id,
                                                                      unnest(upi.preferred_language_ids) AS language_id
                                                               FROM user_profile_info upi
                                                               UNION
                                                               SELECT u_1.id AS user_id,
                                                                      stats.language_id
                                                               FROM contributions_stats_per_language_per_user stats
                                                                        JOIN iam.users u_1 ON u_1.github_user_id = stats.contributor_id),
                                            user_categories AS (SELECT upi.id                             AS user_id,
                                                                       unnest(upi.preferred_category_ids) AS category_id
                                                                FROM user_profile_info upi),
                                            similar_languages AS (SELECT pl.project_id,
                                                                         ul.user_id,
                                                                         array_agg(ul.language_id) AS languages,
                                                                         count(ul.language_id)     AS language_count
                                                                  FROM user_languages ul
                                                                           LEFT JOIN project_languages pl ON pl.language_id = ul.language_id
                                                                  GROUP BY pl.project_id, ul.user_id),
                                            similar_categories AS (SELECT pc.project_id,
                                                                          uc.user_id,
                                                                          array_agg(uc.category_id) AS categories,
                                                                          count(uc.category_id)     AS category_count
                                                                   FROM user_categories uc
                                                                            LEFT JOIN projects_project_categories pc
                                                                                      ON pc.project_category_id = uc.category_id
                                                                   GROUP BY pc.project_id, uc.user_id)
                                       SELECT u.id                                                    AS user_id,
                                              u.github_user_id,
                                              p.id                                                    AS project_id,
                                              COALESCE(sl.languages, '{}'::uuid[])                    AS similar_languages,
                                              COALESCE(sl.language_count, 0::bigint)                  AS similar_language_count,
                                              COALESCE(sc.categories, '{}'::uuid[])                   AS similar_categories,
                                              COALESCE(sc.category_count, 0::bigint)                  AS similar_category_count,
                                              100000 * COALESCE(sl.language_count, 0::bigint) +
                                              10000 * COALESCE(sc.category_count, 0::bigint) + p.rank AS score
                                       FROM similar_languages sl
                                                FULL JOIN similar_categories sc
                                                          ON sl.project_id = sc.project_id AND sl.user_id = sc.user_id
                                                JOIN projects p ON p.id = COALESCE(sl.project_id, sc.project_id)
                                                JOIN iam.users u ON u.id = COALESCE(sl.user_id, sc.user_id)
                                       WHERE NOT (EXISTS (SELECT 1
                                                          FROM projects_contributors pc
                                                          WHERE pc.project_id = p.id
                                                            AND pc.github_user_id = u.github_user_id))
                                         AND (EXISTS (SELECT 1
                                                      FROM projects_good_first_issues pgfi
                                                      WHERE pgfi.project_id = p.id)))
      SELECT md5(ROW (pr.project_id, pr.github_user_id)::text) AS pk,
             pr.project_id,
             pr.github_user_id,
             pr.user_id,
             pr.rank
      FROM (SELECT pr_1.project_id,
                   pr_1.github_user_id,
                   pr_1.user_id,
                   row_number() OVER (PARTITION BY pr_1.github_user_id ORDER BY pr_1.score DESC NULLS LAST) AS rank
            FROM iam.users u
                     JOIN project_recommendations pr_1 ON pr_1.github_user_id = u.github_user_id) pr
      WHERE pr.rank <= 20) v;

create materialized view if not exists m_user_project_recommendations as
SELECT v_user_project_recommendations.pk,
       v_user_project_recommendations.project_id,
       v_user_project_recommendations.github_user_id,
       v_user_project_recommendations.user_id,
       v_user_project_recommendations.rank,
       v_user_project_recommendations.hash
FROM v_user_project_recommendations;

create unique index if not exists public_m_user_project_recommendations_pk
    on m_user_project_recommendations (pk);

create unique index if not exists public_m_user_project_recommendations_pk_hash_idx
    on m_user_project_recommendations (pk, hash);

create materialized view if not exists m_programs_projects as
SELECT v_programs_projects.program_id,
       v_programs_projects.project_id,
       v_programs_projects.has_remaining_grants
FROM v_programs_projects;

create unique index if not exists m_programs_projects_program_id_project_id_idx
    on m_programs_projects (program_id, project_id);

CREATE VIEW users_ecosystems_ranks AS
select stats.ecosystem_id                                                                       as ecosystem_id,
       stats.contributor_id                                                                     as contributor_id,
       rank()
       OVER (PARTITION BY stats.ecosystem_id ORDER BY stats.contribution_count DESC NULLS LAST) as rank,
       percent_rank()
       OVER (PARTITION BY stats.ecosystem_id ORDER BY stats.contribution_count DESC NULLS LAST) as rank_percentile
from contributions_stats_per_ecosystem_per_user stats
;

create or replace view united_stats_per_ecosystem_per_user
            (github_user_id, ecosystem_id, contribution_count, reward_count, pending_request_reward_count, usd_total,
             max_usd, rank, ecosystem_name)
as
SELECT COALESCE(c.contributor_id, r.recipient_id, ranks.contributor_id) AS github_user_id,
       e.id                                                             AS ecosystem_id,
       COALESCE(c.contribution_count, 0::bigint)                        AS contribution_count,
       COALESCE(r.reward_count, 0::bigint)                              AS reward_count,
       COALESCE(r.pending_request_reward_count, 0::bigint)              AS pending_request_reward_count,
       COALESCE(r.usd_total, 0::numeric)                                AS usd_total,
       COALESCE(r.max_usd, 0::numeric)                                  AS max_usd,
       ranks.rank,
       e.name                                                           AS ecosystem_name
FROM contributions_stats_per_ecosystem_per_user c
         FULL JOIN received_rewards_stats_per_ecosystem_per_user r
                   ON r.recipient_id = c.contributor_id AND r.ecosystem_id = c.ecosystem_id
         FULL JOIN users_ecosystems_ranks ranks ON ranks.contributor_id = COALESCE(c.contributor_id, r.recipient_id) AND
                                                   ranks.ecosystem_id = COALESCE(c.ecosystem_id, r.ecosystem_id)
         JOIN ecosystems e ON e.id = COALESCE(c.ecosystem_id, r.ecosystem_id, ranks.ecosystem_id);

create or replace view users_languages_ranks(language_id, contributor_id, rank, rank_percentile) as
SELECT stats.language_id,
       stats.contributor_id,
       rank() OVER (PARTITION BY stats.language_id ORDER BY stats.contribution_count DESC NULLS LAST)         AS rank,
       percent_rank()
       OVER (PARTITION BY stats.language_id ORDER BY stats.contribution_count DESC NULLS LAST)                AS rank_percentile
FROM contributions_stats_per_language_per_user stats;

create or replace view united_stats_per_language_per_user
            (github_user_id, language_id, contribution_count, reward_count, pending_request_reward_count, usd_total,
             max_usd, rank, language_name)
as
SELECT COALESCE(c.contributor_id, r.recipient_id, ranks.contributor_id) AS github_user_id,
       l.id                                                             AS language_id,
       COALESCE(c.contribution_count, 0::bigint)                        AS contribution_count,
       COALESCE(r.reward_count, 0::bigint)                              AS reward_count,
       COALESCE(r.pending_request_reward_count, 0::bigint)              AS pending_request_reward_count,
       COALESCE(r.usd_total, 0::numeric)                                AS usd_total,
       COALESCE(r.max_usd, 0::numeric)                                  AS max_usd,
       ranks.rank,
       l.name                                                           AS language_name
FROM contributions_stats_per_language_per_user c
         FULL JOIN received_rewards_stats_per_language_per_user r
                   ON r.recipient_id = c.contributor_id AND r.language_id = c.language_id
         FULL JOIN users_languages_ranks ranks ON ranks.contributor_id = COALESCE(c.contributor_id, r.recipient_id) AND
                                                  ranks.language_id = COALESCE(c.language_id, r.language_id)
         JOIN languages l ON l.id = COALESCE(c.language_id, r.language_id, ranks.language_id);

create or replace view hackathon_issues(hackathon_id, issue_id, project_ids) as
SELECT DISTINCT h.id                      AS hackathon_id,
                i.id                      AS issue_id,
                array_agg(pgr.project_id) AS project_ids
FROM indexer_exp.github_issues i
         JOIN project_github_repos pgr ON pgr.github_repo_id = i.repo_id
         JOIN indexer_exp.github_issues_labels gil ON i.id = gil.issue_id
         JOIN indexer_exp.github_labels gl ON gil.label_id = gl.id
         JOIN hackathon_projects hp ON hp.project_id = pgr.project_id
         JOIN hackathons h ON (gl.name = ANY (h.github_labels)) AND hp.hackathon_id = h.id
GROUP BY h.id, i.id;

create or replace view hackathon_issue_counts(hackathon_id, issue_count, open_issue_count) as
SELECT hi.hackathon_id,
       count(DISTINCT i.id)                                                                                           AS issue_count,
       count(DISTINCT i.id) FILTER (WHERE i.status = 'OPEN'::indexer_exp.github_issue_status AND
                                          gia.user_id IS NULL)                                                        AS open_issue_count
FROM hackathon_issues hi
         JOIN indexer_exp.github_issues i ON i.id = hi.issue_id
         LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
GROUP BY hi.hackathon_id;

create or replace view completed_contributions
            (id, repo_id, contributor_id, type, status, pull_request_id, issue_id, code_review_id, created_at,
             completed_at, tech_created_at, tech_updated_at, github_number, github_status, github_title,
             github_html_url, github_body, github_comments_count, repo_owner_login, repo_name, repo_html_url,
             github_author_id, github_author_login, github_author_html_url, github_author_avatar_url, contributor_login,
             contributor_html_url, contributor_avatar_url, pr_review_state, main_file_extensions, project_ids)
as
SELECT c.id,
       c.repo_id,
       c.contributor_id,
       c.type,
       c.status,
       c.pull_request_id,
       c.issue_id,
       c.code_review_id,
       c.created_at,
       c.completed_at,
       c.tech_created_at,
       c.tech_updated_at,
       c.github_number,
       c.github_status,
       c.github_title,
       c.github_html_url,
       c.github_body,
       c.github_comments_count,
       c.repo_owner_login,
       c.repo_name,
       c.repo_html_url,
       c.github_author_id,
       c.github_author_login,
       c.github_author_html_url,
       c.github_author_avatar_url,
       c.contributor_login,
       c.contributor_html_url,
       c.contributor_avatar_url,
       c.pr_review_state,
       c.main_file_extensions,
       array_agg(DISTINCT p.id) AS project_ids
FROM indexer_exp.contributions c
         JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id
         JOIN projects p ON p.id = pgr.project_id
WHERE c.status = 'COMPLETED'::indexer_exp.contribution_status
GROUP BY c.id;


CREATE OR REPLACE VIEW public.project_technologies AS
SELECT pgr.project_id      as project_id,
       grl.language        as technology,
       sum(grl.line_count) as line_count
FROM project_github_repos pgr
         JOIN indexer_exp.github_repo_languages grl ON grl.repo_id = pgr.github_repo_id
         JOIN indexer_exp.github_repos gr on gr.id = pgr.github_repo_id AND gr.visibility = 'PUBLIC'
GROUP BY pgr.project_id, grl.language;


CREATE OR REPLACE VIEW registered_users AS
SELECT u.id,
       u.github_user_id,
       COALESCE(ga.login, u.github_login)                                   AS login,
       COALESCE(ga.avatar_url, u.github_avatar_url)                         AS avatar_url,
       COALESCE(ga.html_url, 'https://github.com/'::text || u.github_login) AS html_url,
       u.email::citext                                                      AS email,
       u.last_seen_at                                                       AS last_seen,
       CASE WHEN 'ADMIN' = ANY (u.roles) THEN true ELSE false END           AS admin
FROM iam.users u
         LEFT JOIN indexer_exp.github_accounts ga ON ga.id = u.github_user_id;

CREATE OR REPLACE VIEW public.project_stats_for_ranking_computation AS
with budget_stats as (select pa.project_id,
                             coalesce(sum(pa.current_allowance) filter ( where c.code = 'USD' ), 0)               usd_remaining_amount,
                             coalesce(sum(pa.current_allowance) filter ( where c.code = 'USDC' ), 0)              usdc_remaining_amount,
                             coalesce(sum(pa.current_allowance * luq.price) filter ( where c.code = 'USDC' ), 0)  usdc_dollars_equivalent_remaining_amount,
                             coalesce(sum(pa.current_allowance) filter ( where c.code = 'OP' ), 0)                op_remaining_amount,
                             coalesce(sum(pa.current_allowance * luq.price) filter ( where c.code = 'OP' ), 0)    op_dollars_equivalent_remaining_amount,
                             coalesce(sum(pa.current_allowance) filter ( where c.code = 'STRK' ), 0)              stark_remaining_amount,
                             coalesce(sum(pa.current_allowance * luq.price) filter ( where c.code = 'STRK' ), 0)  stark_dollars_equivalent_remaining_amount,
                             coalesce(sum(pa.current_allowance) filter ( where c.code = 'APT' ), 0)               apt_remaining_amount,
                             coalesce(sum(pa.current_allowance * luq.price) filter ( where c.code = 'APT' ), 0)   apt_dollars_equivalent_remaining_amount,
                             coalesce(sum(pa.current_allowance) filter ( where c.code = 'ETH' ), 0)               eth_remaining_amount,
                             coalesce(sum(pa.current_allowance * luq.price) filter ( where c.code = 'ETH' ), 0)   eth_dollars_equivalent_remaining_amount,
                             coalesce(sum(pa.current_allowance) filter ( where c.code = 'LORDS' ), 0)             lords_remaining_amount,
                             coalesce(sum(pa.current_allowance * luq.price) filter ( where c.code = 'LORDS' ), 0) lords_dollars_equivalent_remaining_amount
                      from project_allowances pa
                               join currencies c on c.id = pa.currency_id
                               left join accounting.latest_usd_quotes luq on luq.currency_id = pa.currency_id
                      group by pa.project_id),
     reward_stats as (select r.project_id,
                             count(distinct r.recipient_id)                                           distinct_recipient_number_last_1_month,
                             coalesce(sum(r.amount) filter ( where c.code = 'USD' ), 0)               usd_spent_amount_last_1_month,
                             coalesce(sum(r.amount) filter ( where c.code = 'USDC' ), 0)              usdc_spent_amount_last_1_month,
                             coalesce(sum(r.amount * luq.price) filter ( where c.code = 'USDC' ), 0)  usdc_spent_amount_dollars_equivalent_last_1_month,
                             coalesce(sum(r.amount) filter ( where c.code = 'OP' ), 0)                op_spent_amount_last_1_month,
                             coalesce(sum(r.amount * luq.price) filter ( where c.code = 'OP' ), 0)    op_spent_amount_dollars_equivalent_last_1_month,
                             coalesce(sum(r.amount) filter ( where c.code = 'ETH' ), 0)               eth_spent_amount_last_1_month,
                             coalesce(sum(r.amount * luq.price) filter ( where c.code = 'ETH' ), 0)   eth_spent_amount_dollars_equivalent_last_1_month,
                             coalesce(sum(r.amount) filter ( where c.code = 'APT' ), 0)               apt_spent_amount_last_1_month,
                             coalesce(sum(r.amount * luq.price) filter ( where c.code = 'APT' ), 0)   apt_spent_amount_dollars_equivalent_last_1_month,
                             coalesce(sum(r.amount) filter ( where c.code = 'STRK' ), 0)              stark_spent_amount_last_1_month,
                             coalesce(sum(r.amount * luq.price) filter ( where c.code = 'STRK' ), 0)  stark_spent_amount_dollars_equivalent_last_1_month,
                             coalesce(sum(r.amount) filter ( where c.code = 'LORDS' ), 0)             lords_spent_amount_last_1_month,
                             coalesce(sum(r.amount * luq.price) filter ( where c.code = 'LORDS' ), 0) lords_spent_amount_dollars_equivalent_last_1_month
                      from rewards r
                               join currencies c on c.id = r.currency_id
                               left join accounting.latest_usd_quotes luq on luq.currency_id = r.currency_id
                      where r.requested_at > CURRENT_DATE - INTERVAL '1 months'
                      group by r.project_id),
     contribution_stats as (select pgr.project_id,
                                   coalesce(sum(1) filter ( where c.type = 'PULL_REQUEST' ), 0)                                      pr_count,
                                   coalesce(sum(1) filter ( where c.type = 'PULL_REQUEST' and
                                                                  c.created_at > CURRENT_DATE - INTERVAL '3 months'), 0)             pr_count_last_3_months,
                                   coalesce(sum(1) filter ( where c.type = 'PULL_REQUEST' and c.status = 'IN_PROGRESS' ), 0)         open_pr_count,
                                   coalesce(sum(1) filter ( where c.type = 'ISSUE' ), 0)                                             issue_count,
                                   coalesce(sum(1) filter ( where c.type = 'ISSUE' and
                                                                  c.created_at > CURRENT_DATE - INTERVAL '3 months'), 0)             issue_count_last_3_months,
                                   coalesce(sum(1) filter ( where c.type = 'ISSUE' and c.status = 'IN_PROGRESS'), 0)                 open_issue_count,
                                   coalesce(sum(1) filter ( where c.type = 'CODE_REVIEW' ), 0)                                       cr_count,
                                   coalesce(sum(1) filter ( where c.type = 'CODE_REVIEW' and
                                                                  c.created_at > CURRENT_DATE - INTERVAL '3 months'), 0)             cr_count_last_3_months,
                                   coalesce(sum(1) filter ( where c.type = 'CODE_REVIEW' and c.status = 'IN_PROGRESS' ), 0)          open_cr_count,
                                   count(distinct c.contributor_id) FILTER (WHERE c.created_at > (CURRENT_DATE - '1 mon'::interval)) contributor_count
                            from project_github_repos pgr
                                     join indexer_exp.github_repos gr
                                          on pgr.github_repo_id = gr.id and gr.visibility = 'PUBLIC'
                                     join indexer_exp.contributions c on c.repo_id = gr.id
                            group by pgr.project_id)
select pd2.id,
       pd2.created_at,
       coalesce(cs.pr_count, 0)                                           as pr_count,
       coalesce(cs.pr_count_last_3_months, 0)                             as pr_count_last_3_months,
       coalesce(cs.open_pr_count, 0)                                      as open_pr_count,
       coalesce(cs.issue_count, 0)                                        as issue_count,
       coalesce(cs.issue_count_last_3_months, 0)                          as issue_count_last_3_months,
       coalesce(cs.open_issue_count, 0)                                   as open_issue_count,
       coalesce(cs.cr_count, 0)                                           as cr_count,
       coalesce(cs.cr_count_last_3_months, 0)                             as cr_count_last_3_months,
       coalesce(cs.open_cr_count, 0)                                      as open_cr_count,
       coalesce(cs.contributor_count, 0)                                  as contributor_count,
       coalesce(rs.distinct_recipient_number_last_1_month, 0)             as distinct_recipient_number_last_1_months,
       coalesce(rs.usd_spent_amount_last_1_month, 0)                      as usd_spent_amount,
       coalesce(rs.usdc_spent_amount_last_1_month, 0)                     as usdc_spent_amount,
       coalesce(rs.usdc_spent_amount_dollars_equivalent_last_1_month, 0)  as usdc_spent_amount_dollars_equivalent,
       coalesce(rs.op_spent_amount_last_1_month, 0)                       as op_spent_amount,
       coalesce(rs.op_spent_amount_dollars_equivalent_last_1_month, 0)    as op_spent_amount_dollars_equivalent,
       coalesce(rs.eth_spent_amount_last_1_month, 0)                      as eth_spent_amount,
       coalesce(rs.eth_spent_amount_dollars_equivalent_last_1_month, 0)   as eth_spent_amount_dollars_equivalent,
       coalesce(rs.apt_spent_amount_last_1_month, 0)                      as apt_spent_amount,
       coalesce(rs.apt_spent_amount_dollars_equivalent_last_1_month, 0)   as apt_spent_amount_dollars_equivalent,
       coalesce(rs.stark_spent_amount_last_1_month, 0)                    as stark_spent_amount,
       coalesce(rs.stark_spent_amount_dollars_equivalent_last_1_month, 0) as stark_spent_amount_dollars_equivalent,
       coalesce(rs.lords_spent_amount_last_1_month, 0)                    as lords_spent_amount,
       coalesce(rs.lords_spent_amount_dollars_equivalent_last_1_month, 0) as lords_spent_amount_dollars_equivalent,
       coalesce(rs.stark_spent_amount_dollars_equivalent_last_1_month +
                rs.op_spent_amount_dollars_equivalent_last_1_month +
                rs.lords_spent_amount_dollars_equivalent_last_1_month +
                rs.apt_spent_amount_dollars_equivalent_last_1_month + rs.usd_spent_amount_last_1_month +
                rs.usdc_spent_amount_dollars_equivalent_last_1_month +
                rs.eth_spent_amount_dollars_equivalent_last_1_month,
                0)                                                        as total_dollars_equivalent_spent_last_1_month,
       coalesce(bs.usd_remaining_amount, 0)                               as usd_remaining_amount,
       coalesce(bs.op_remaining_amount, 0)                                as op_remaining_amount,
       coalesce(bs.stark_remaining_amount, 0)                             as stark_remaining_amount,
       coalesce(bs.apt_remaining_amount, 0)                               as apt_remaining_amount,
       coalesce(bs.eth_remaining_amount, 0)                               as eth_remaining_amount,
       coalesce(bs.lords_remaining_amount, 0)                             as lords_remaining_amount,
       coalesce(bs.usd_remaining_amount, 0)                               as usdc_remaining_amount,
       coalesce(bs.apt_dollars_equivalent_remaining_amount +
                bs.usd_remaining_amount +
                bs.usdc_dollars_equivalent_remaining_amount +
                bs.op_dollars_equivalent_remaining_amount +
                bs.eth_dollars_equivalent_remaining_amount +
                bs.lords_dollars_equivalent_remaining_amount +
                bs.stark_dollars_equivalent_remaining_amount, 0)          as total_dollars_equivalent_remaining_amount
from projects pd2
         left join contribution_stats cs on cs.project_id = pd2.id
         left join reward_stats rs on rs.project_id = pd2.id
         left join budget_stats bs on bs.project_id = pd2.id
where (EXISTS(select 1
              from project_github_repos pgr2
                       join indexer_exp.github_repos gr2
                            on gr2.id = pgr2.github_repo_id and
                               pgr2.project_id = pd2.id and
                               gr2.visibility = 'PUBLIC'));


create view ecosystem_languages as
select distinct pe.ecosystem_id as ecosystem_id,
                pl.language_id  as language_id
from projects_ecosystems pe
         join project_languages pl on pe.project_id = pl.project_id;


create view ecosystem_project_categories as
select distinct pe.ecosystem_id         as ecosystem_id,
                ppc.project_category_id as project_category_id
from projects_ecosystems pe
         join projects_project_categories ppc on ppc.project_id = pe.project_id;

create view public_projects as
select id,
       slug,
       rank,
       name,
       logo_url,
       short_description,
       long_description,
       hiring,
       reward_ignore_pull_requests_by_default,
       reward_ignore_issues_by_default,
       reward_ignore_code_reviews_by_default,
       reward_ignore_contributions_before_date_by_default
from projects
where visibility = 'PUBLIC';

create view recent_public_activity as
(select distinct on (c.repo_id) cast('PULL_REQUEST' as activity_type) as type,
                                c.completed_at                        as timestamp,
                                c.github_author_id                    as pull_request_author_id,
                                c.project_ids[1]                      as project_id,
                                cast(NULL as uuid)                    as reward_id
 from (select distinct on (c.github_author_id) c.*
       from (select distinct on (c.pull_request_id) c.*
             from public_contributions c
                      join iam.users u on u.github_user_id = c.github_author_id
             where c.completed_at > now() - interval '1 week'
               and c.type = 'PULL_REQUEST'
               and c.status = 'COMPLETED'
               and array_length(c.main_file_extensions, 1) > 0) c
       order by c.github_author_id, c.completed_at desc) c
 order by c.repo_id, c.completed_at desc)

UNION

(select distinct on (r.requestor_id) cast('REWARD_CREATED' as activity_type) as type,
                                     r.requested_at                          as timestamp,
                                     cast(NULL as bigint)                    as pull_request_author_id,
                                     r.project_id                            as project_id,
                                     r.id                                    as reward_id
 from public_received_rewards r
 where r.requested_at > now() - interval '1 week'
 order by r.requestor_id, r.requested_at desc)

UNION

(select distinct on (r.invoice_id) cast('REWARD_CLAIMED' as activity_type) as type,
                                   r.invoice_received_at                   as timestamp,
                                   cast(NULL as bigint)                    as pull_request_author_id,
                                   r.project_id                            as project_id,
                                   r.id                                    as reward_id
 from public_received_rewards r
 where r.invoice_received_at > now() - interval '1 week'
 order by r.invoice_id, r.invoice_received_at desc)

UNION

(select cast('PROJECT_CREATED' as activity_type) as type,
        p.created_at                             as timestamp,
        cast(NULL as bigint)                     as pull_request_author_id,
        p.id                                     as project_id,
        cast(NULL as uuid)                       as reward_id
 from projects p
 where p.created_at > now() - interval '1 week'
 order by p.created_at desc)
;

CREATE VIEW united_stats_per_user AS
SELECT coalesce(c.contributor_id, r.recipient_id, ranks.github_user_id) as github_user_id,
       coalesce(c.contribution_count, 0)                                as contribution_count,
       coalesce(r.reward_count, 0)                                      as reward_count,
       coalesce(r.pending_request_reward_count, 0)                      as pending_request_reward_count,
       coalesce(r.usd_total, 0)                                         as usd_total,
       coalesce(r.max_usd, 0)                                           as max_usd,
       ranks.rank
FROM contributions_stats_per_user c
         FULL JOIN received_rewards_stats_per_user r
                   ON r.recipient_id = c.contributor_id
         FULL JOIN global_users_ranks ranks
                   ON ranks.github_user_id = coalesce(c.contributor_id, r.recipient_id);

CREATE VIEW project_members AS
SELECT pc.project_id     as project_id,
       pc.github_user_id as github_user_id,
       u.id              as user_id
FROM projects_contributors pc
         LEFT JOIN iam.users u on u.github_user_id = pc.github_user_id
UNION
SELECT pl.project_id    as project_id,
       u.github_user_id as github_user_id,
       pl.user_id       as user_id
FROM project_leads pl
         LEFT JOIN iam.users u on u.id = pl.user_id;


create index if not exists rewards_project_id_index
    on rewards (project_id);

create index if not exists rewards_billing_profile_id_index
    on rewards (billing_profile_id);

create index if not exists rewards_invoice_id_index
    on rewards (invoice_id);

create index if not exists rewards_recipient_id_index
    on rewards (recipient_id);

create index if not exists rewards_requestor_id_index
    on rewards (requestor_id);