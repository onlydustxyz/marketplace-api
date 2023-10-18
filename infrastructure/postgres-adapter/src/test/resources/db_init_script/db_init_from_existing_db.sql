-- This migration is needed so that hasura auth can populate its auth schema

-- It is duplicated in migrations/2022-11-15-145459_create-hasura-auth-schema/up.sql
-- so that it runs also on deploy in Heroku

-- auth schema
CREATE SCHEMA IF NOT EXISTS auth;
-- https://github.com/hasura/graphql-engine/issues/3657
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;
CREATE OR REPLACE FUNCTION public.set_current_timestamp_updated_at() RETURNS trigger
    LANGUAGE plpgsql AS
$$
declare
    _new record;
begin
    _new := new;
    _new."updated_at" = now();
    return _new;
end;
$$;

create aggregate public.jsonb_concat_agg(jsonb) (
    sfunc = jsonb_concat,
    stype = jsonb,
    initcond = '{}'
    );

create schema api;

create type public.project_visibility as enum ('PUBLIC', 'PRIVATE');

create type public.contact_channel as enum ('email', 'telegram', 'twitter', 'discord', 'linkedin', 'whatsapp');

create type public.allocated_time as enum ('none', 'less_than_one_day', 'one_to_three_days', 'greater_than_three_days');

create type public.profile_cover as enum ('cyan', 'magenta', 'yellow', 'blue');

create type public.github_pull_request_status as enum ('open', 'closed', 'merged');

create type public.github_issue_status as enum ('open', 'completed', 'cancelled');

create type public.github_ci_checks as enum ('passed', 'failed');

create type public.github_code_review_status as enum ('pending', 'completed');

create type public.github_code_review_outcome as enum ('change_requested', 'approved');

create type public.contribution_type as enum ('issue', 'pull_request', 'code_review');

create type public.contribution_status as enum ('in_progress', 'complete', 'canceled');

create type public.wallet_type as enum ('address', 'name');

create type public.network as enum ('ethereum', 'aptos', 'starknet', 'optimism');

create type public.preferred_method as enum ('fiat', 'crypto');

create type public.currency as enum ('usd', 'eth', 'op', 'apt', 'stark');

create table public.__diesel_schema_migrations
(
    version varchar(50)                         not null
        primary key,
    run_on  timestamp default CURRENT_TIMESTAMP not null
);

create table public.budgets
(
    id               uuid default gen_random_uuid() not null
        primary key,
    initial_amount   numeric                        not null,
    remaining_amount numeric                        not null,
    currency         currency                       not null
);

create unique index budget_currency_idx
    on public.budgets (currency, id);

create table public.event_deduplications
(
    deduplication_id text              not null
        primary key,
    event_index      integer default 0 not null
);

create table public.events
(
    index          serial
        primary key,
    timestamp      timestamp not null,
    aggregate_name varchar   not null,
    aggregate_id   varchar   not null,
    payload        jsonb     not null,
    metadata       jsonb,
    command_id     uuid
);

create index events_aggregate_idx
    on public.events (aggregate_id, aggregate_name, timestamp, index);

create index events_aggregate_name_idx
    on public.events (aggregate_name, timestamp, index);

create table public.payment_requests
(
    id                  uuid    default gen_random_uuid() not null
        constraint payment_requests_pkey1
            primary key,
    requestor_id        uuid                              not null,
    recipient_id        bigint                            not null,
    amount              numeric                           not null,
    requested_at        timestamp                         not null,
    invoice_received_at timestamp,
    hours_worked        integer default 0                 not null,
    project_id          uuid                              not null,
    currency            currency                          not null
);

create index payment_requests_requestor_id_idx
    on public.payment_requests (requestor_id);

create index payment_requests_recipient_id_idx
    on public.payment_requests (recipient_id);

create unique index payment_requests_project_id_idx
    on public.payment_requests (project_id, id);

create table public.payments
(
    id            uuid default gen_random_uuid() not null
        primary key,
    amount        numeric                        not null,
    currency_code text                           not null,
    receipt       jsonb                          not null,
    request_id    uuid                           not null,
    processed_at  timestamp                      not null
);

create index payments_request_id_idx
    on public.payments (request_id);

create table public.project_details
(
    project_id        uuid                     not null
        primary key,
    telegram_link     text,
    logo_url          text,
    name              text    default ''::text not null,
    short_description text    default ''::text not null,
    long_description  text    default ''::text not null,
    hiring            boolean default false    not null,
    rank              integer default 0        not null,
    visibility        project_visibility       not null,
    key               text generated always as (lower(replace(
            regexp_replace(name, '[^a-zA-Z0-9_\-\ ]+'::text, ''::text, 'g'::text), ' '::text,
            '-'::text))) stored                not null
);

create index project_details_rank_idx
    on public.project_details (rank desc);

create index project_details_name_idx
    on public.project_details (name);

create unique index project_details_key
    on public.project_details (key);

create table public.project_github_repos
(
    project_id     uuid   not null,
    github_repo_id bigint not null,
    primary key (project_id, github_repo_id)
);

create unique index project_github_repos_github_repo_id_project_id_idx
    on public.project_github_repos (github_repo_id, project_id);

create table public.project_leads
(
    project_id  uuid                    not null,
    user_id     uuid                    not null,
    assigned_at timestamp default now() not null,
    primary key (project_id, user_id)
);

create unique index project_leads_user_id_project_id_idx
    on public.project_leads (user_id, project_id);

create table public.projects
(
    id uuid not null
        primary key
);

create table public.pending_project_leader_invitations
(
    id             uuid default gen_random_uuid() not null
        primary key,
    project_id     uuid                           not null
        references public.projects,
    github_user_id bigint                         not null,
    constraint pending_project_leader_invitation_project_id_github_user_id_key
        unique (project_id, github_user_id)
);

create unique index pending_project_leader_invitation_github_user_id_project_id_idx
    on public.pending_project_leader_invitations (github_user_id, project_id);

create table public.sponsors
(
    id       uuid default gen_random_uuid() not null
        primary key,
    name     text                           not null
        unique,
    logo_url text                           not null,
    url      text
);

create table public.projects_sponsors
(
    project_id uuid not null
        references public.projects
            on delete cascade,
    sponsor_id uuid not null
        references public.sponsors
            on delete cascade,
    primary key (project_id, sponsor_id),
    unique (project_id, sponsor_id)
);

create table public.user_payout_info
(
    user_id              uuid not null
        constraint user_info_pkey
            primary key,
    identity             jsonb,
    location             jsonb,
    usd_preferred_method preferred_method
);

create table public.work_items
(
    payment_id   uuid              not null,
    number       bigint            not null,
    repo_id      bigint default 0  not null,
    id           text              not null,
    type         contribution_type not null,
    project_id   uuid              not null,
    recipient_id bigint            not null,
    primary key (payment_id, repo_id, number)
);

create table public.github_repos
(
    id          bigint                      not null
        constraint crm_github_repos_pkey
            primary key,
    owner       text                        not null,
    name        text                        not null,
    updated_at  timestamp,
    description text    default ''::text    not null,
    stars       integer default 0           not null,
    fork_count  integer default 0           not null,
    html_url    text    default ''::text    not null,
    languages   jsonb   default '{}'::jsonb not null,
    parent_id   bigint,
    has_issues  boolean default true        not null
);

create table public.github_repo_indexes
(
    repo_id                     bigint not null
        primary key,
    repo_indexer_state          jsonb,
    issues_indexer_state        jsonb,
    pull_requests_indexer_state jsonb,
    indexed_at                  timestamp
);

create table public.github_issues
(
    id             bigint                     not null
        constraint github_pulls_pkey
            primary key,
    repo_id        bigint                     not null,
    number         bigint                     not null,
    created_at     timestamp                  not null,
    author_id      bigint                     not null,
    status         github_issue_status        not null,
    title          text                       not null,
    html_url       text                       not null,
    closed_at      timestamp,
    assignee_ids   jsonb  default '[]'::jsonb not null,
    comments_count bigint default 0           not null
);

create index github_issues_repo_id_issue_number_idx
    on public.github_issues (repo_id, number);

create index github_issues_created_at_idx
    on public.github_issues (created_at);

create index github_issues_author_id_idx
    on public.github_issues (author_id);

create table public.github_users
(
    id         bigint not null
        primary key,
    login      text   not null,
    avatar_url text   not null,
    html_url   text   not null,
    bio        text,
    location   text,
    website    text,
    twitter    text,
    linkedin   text,
    telegram   text
);

create index github_users_login_idx
    on public.github_users (login);

create table public.github_user_indexes
(
    user_id                   bigint not null
        primary key,
    user_indexer_state        jsonb,
    contributor_indexer_state jsonb,
    indexed_at                timestamp
);

create table public.applications
(
    id           uuid      not null
        primary key,
    received_at  timestamp not null,
    project_id   uuid      not null,
    applicant_id uuid      not null
);

create index applications_project_id_applicant_id_idx
    on public.applications (project_id, applicant_id);

create table public.user_profile_info
(
    id                    uuid                                          not null
        primary key,
    bio                   text,
    location              text,
    website               text,
    languages             jsonb,
    weekly_allocated_time allocated_time default 'none'::allocated_time not null,
    looking_for_a_job     boolean        default false                  not null,
    avatar_url            text,
    cover                 profile_cover
);

create table public.onboardings
(
    user_id                              uuid not null
        constraint terms_and_conditions_acceptances_pkey
            primary key,
    terms_and_conditions_acceptance_date timestamp,
    profile_wizard_display_date          timestamp
);

create table public.commands
(
    id               uuid                          not null
        primary key,
    processing_count integer                       not null,
    created_at       timestamp default now()       not null,
    updated_at       timestamp,
    metadata         jsonb     default '{}'::jsonb not null
);

create table public.contact_informations
(
    user_id uuid            not null,
    channel contact_channel not null,
    contact text            not null,
    public  boolean         not null,
    primary key (user_id, channel)
);

create table public.projects_contributors
(
    project_id     uuid   not null,
    github_user_id bigint not null,
    primary key (project_id, github_user_id)
);

create unique index projects_contributors_github_user_id_project_id_idx
    on public.projects_contributors (github_user_id, project_id);

create table public.auth_users
(
    id                   uuid                  not null
        primary key,
    github_user_id       bigint,
    email                citext,
    last_seen            timestamp,
    login_at_signup      text                  not null,
    avatar_url_at_signup text,
    created_at           timestamp             not null,
    admin                boolean default false not null
);

create unique index auth_users_github_user_id_idx
    on public.auth_users (github_user_id)
    where (github_user_id IS NOT NULL);

create index auth_users_login_at_signup_idx
    on public.auth_users (login_at_signup);

create table public.technologies
(
    technology text not null
        primary key
);

create table public.github_pull_requests
(
    id         bigint                     not null
        primary key,
    repo_id    bigint                     not null,
    number     bigint                     not null,
    created_at timestamp                  not null,
    author_id  bigint                     not null,
    merged_at  timestamp,
    status     github_pull_request_status not null,
    title      text                       not null,
    html_url   text                       not null,
    closed_at  timestamp,
    draft      boolean default false      not null,
    ci_checks  github_ci_checks
);

create table public.github_pull_request_commits
(
    sha             text   not null,
    pull_request_id bigint not null,
    html_url        text   not null,
    author_id       bigint not null,
    primary key (pull_request_id, sha)
);

create table public.github_pull_request_reviews
(
    pull_request_id bigint                    not null,
    reviewer_id     bigint                    not null,
    status          github_code_review_status not null,
    outcome         github_code_review_outcome,
    submitted_at    timestamp,
    id              text                      not null
        primary key
);

create index github_pull_request_reviews_pull_request
    on public.github_pull_request_reviews (pull_request_id);

create table public.contributions
(
    repo_id    bigint              not null,
    user_id    bigint              not null,
    type       contribution_type   not null,
    details_id text                not null,
    status     contribution_status not null,
    created_at timestamp           not null,
    closed_at  timestamp,
    id         text                not null
        primary key
);

create index contributions_user_id_idx
    on public.contributions (user_id);

create index contributions_repo_id_idx
    on public.contributions (repo_id);

create index contributions_type_idx
    on public.contributions (type);

create table public.github_pull_request_indexes
(
    pull_request_id            bigint not null
        primary key,
    pull_request_indexer_state jsonb
);

create table public.projects_rewarded_users
(
    project_id     uuid    not null,
    github_user_id bigint  not null,
    reward_count   integer not null,
    primary key (project_id, github_user_id)
);

create unique index projects_rewarded_users_github_user_id_project_id_idx
    on public.projects_rewarded_users (github_user_id, project_id);

create table public.projects_pending_contributors
(
    project_id     uuid   not null,
    github_user_id bigint not null,
    primary key (project_id, github_user_id)
);

create unique index projects_pending_contributors_github_user_id_project_id_idx
    on public.projects_pending_contributors (github_user_id, project_id);

create table public.ignored_contributions
(
    project_id      uuid not null,
    contribution_id text not null,
    primary key (project_id, contribution_id)
);

create table public.closing_issues
(
    github_issue_id        bigint not null,
    github_pull_request_id bigint not null,
    primary key (github_issue_id, github_pull_request_id)
);

create table public.wallets
(
    user_id uuid        not null,
    network network     not null,
    type    wallet_type not null,
    address text        not null,
    primary key (user_id, network)
);

create table public.bank_accounts
(
    user_id uuid not null
        primary key,
    bic     text not null,
    iban    text not null
);

create table public.projects_budgets
(
    project_id uuid not null,
    budget_id  uuid not null,
    primary key (project_id, budget_id)
);

create table public.crypto_usd_quotes
(
    currency   currency  not null
        primary key,
    price      numeric   not null,
    updated_at timestamp not null
);

create view api.commands(id, processing_count, created_at, updated_at, project_id) as
SELECT c.id,
       GREATEST(c.processing_count, 0) AS processing_count,
       c.created_at,
       c.updated_at,
       m."Project"                     AS project_id
FROM commands c,
     LATERAL jsonb_to_recordset(c.metadata -> 'aggregates'::text) m("Project" uuid);

create view api.technologies(technology) as
SELECT technologies.technology
FROM technologies;

create view public.registered_users (id, github_user_id, login, avatar_url, html_url, email, last_seen, admin) as
SELECT au.id,
       au.github_user_id,
       COALESCE(gu.login, au.login_at_signup)                                   AS login,
       COALESCE(gu.avatar_url, au.avatar_url_at_signup)                         AS avatar_url,
       COALESCE(gu.html_url, 'https://github.com/'::text || au.login_at_signup) AS html_url,
       au.email,
       au.last_seen,
       au.admin
FROM auth_users au
         LEFT JOIN github_users gu ON gu.id = au.github_user_id;

create view api.contact_informations(github_user_id, channel, contact, public) as
SELECT gu.id                             AS github_user_id,
       'telegram'::contact_channel       AS channel,
       COALESCE(ci.contact, gu.telegram) AS contact,
       COALESCE(ci.public, true)         AS public
FROM github_users gu
         LEFT JOIN auth_users au ON au.github_user_id = gu.id
         LEFT JOIN contact_informations ci ON ci.user_id = au.id AND ci.channel = 'telegram'::contact_channel
UNION
SELECT gu.id                            AS github_user_id,
       'twitter'::contact_channel       AS channel,
       COALESCE(ci.contact, gu.twitter) AS contact,
       COALESCE(ci.public, true)        AS public
FROM github_users gu
         LEFT JOIN auth_users au ON au.github_user_id = gu.id
         LEFT JOIN contact_informations ci ON ci.user_id = au.id AND ci.channel = 'twitter'::contact_channel
UNION
SELECT gu.id                             AS github_user_id,
       'linkedin'::contact_channel       AS channel,
       COALESCE(ci.contact, gu.linkedin) AS contact,
       COALESCE(ci.public, true)         AS public
FROM github_users gu
         LEFT JOIN auth_users au ON au.github_user_id = gu.id
         LEFT JOIN contact_informations ci ON ci.user_id = au.id AND ci.channel = 'linkedin'::contact_channel
UNION
SELECT COALESCE(gu.id, au.github_user_id)   AS github_user_id,
       'email'::contact_channel             AS channel,
       COALESCE(ci.contact, au.email::text) AS contact,
       COALESCE(ci.public, false)           AS public
FROM github_users gu
         FULL JOIN auth_users au ON au.github_user_id = gu.id
         LEFT JOIN contact_informations ci ON ci.user_id = au.id AND ci.channel = 'email'::contact_channel
UNION
SELECT au.github_user_id,
       ci.channel,
       ci.contact,
       ci.public
FROM contact_informations ci
         LEFT JOIN auth_users au ON ci.user_id = au.id
WHERE ci.channel = 'discord'::contact_channel
UNION
SELECT au.github_user_id,
       ci.channel,
       ci.contact,
       ci.public
FROM contact_informations ci
         LEFT JOIN auth_users au ON ci.user_id = au.id
WHERE ci.channel = 'whatsapp'::contact_channel;

create view api.user_profiles
            (github_user_id, login, avatar_url, html_url, user_id, created_at, last_seen, bio, location, website,
             languages, weekly_allocated_time, looking_for_a_job, cover)
as
SELECT COALESCE(gu.id, au.github_user_id)                                       AS github_user_id,
       COALESCE(gu.login, au.login_at_signup)                                   AS login,
       COALESCE(upi.avatar_url, gu.avatar_url, au.avatar_url_at_signup)         AS avatar_url,
       COALESCE(gu.html_url, 'https://github.com/'::text || au.login_at_signup) AS html_url,
       au.id                                                                    AS user_id,
       au.created_at,
       au.last_seen,
       COALESCE(upi.bio, gu.bio)                                                AS bio,
       COALESCE(upi.location, gu.location)                                      AS location,
       COALESCE(upi.website, gu.website)                                        AS website,
       COALESCE(upi.languages, repos_stats.languages)                           AS languages,
       upi.weekly_allocated_time,
       upi.looking_for_a_job,
       upi.cover
FROM github_users gu
         FULL JOIN auth_users au ON au.github_user_id = gu.id
         LEFT JOIN user_profile_info upi ON upi.id = au.id
         LEFT JOIN LATERAL ( SELECT jsonb_concat_agg(gr.languages) AS languages
                             FROM contributions c
                                      JOIN github_repos gr ON gr.id = c.repo_id
                             WHERE c.user_id = gu.id) repos_stats ON 1 = 1;

create view api.completed_contributions
            (id, github_user_id, details_id, type, repo_id, status, closed_at, created_at, project_id) as
SELECT c.id,
       c.user_id AS github_user_id,
       c.details_id,
       c.type,
       c.repo_id,
       c.status,
       c.closed_at,
       c.created_at,
       pgr.project_id
FROM contributions c
         JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id
WHERE c.status = 'complete'::contribution_status;

create view api.contribution_stats
            (github_user_id, project_id, total_count, issue_count, code_review_count, pull_request_count, min_date,
             max_date)
as
SELECT c.github_user_id,
       c.project_id,
       count(*)                                                                      AS total_count,
       count(c.details_id) FILTER (WHERE c.type = 'issue'::contribution_type)        AS issue_count,
       count(c.details_id) FILTER (WHERE c.type = 'code_review'::contribution_type)  AS code_review_count,
       count(c.details_id) FILTER (WHERE c.type = 'pull_request'::contribution_type) AS pull_request_count,
       min(c.created_at)                                                             AS min_date,
       max(c.created_at)                                                             AS max_date
FROM api.completed_contributions c
GROUP BY c.github_user_id, c.project_id;

create view api.contribution_counts (github_user_id, year, week, issue_count, code_review_count, pull_request_count) as
SELECT c.github_user_id,
       date_part('isoyear'::text, c.created_at)                                               AS year,
       date_part('week'::text, c.created_at)                                                  AS week,
       count(DISTINCT c.details_id) FILTER (WHERE c.type = 'issue'::contribution_type)        AS issue_count,
       count(DISTINCT c.details_id) FILTER (WHERE c.type = 'code_review'::contribution_type)  AS code_review_count,
       count(DISTINCT c.details_id) FILTER (WHERE c.type = 'pull_request'::contribution_type) AS pull_request_count
FROM api.completed_contributions c
GROUP BY c.github_user_id, (date_part('isoyear'::text, c.created_at)), (date_part('week'::text, c.created_at));

create view api.github_repos
            (id, owner, name, description, stars, fork_count, html_url, languages, parent_id, has_issues, updated_at,
             indexed_at)
as
SELECT r.id,
       r.owner,
       r.name,
       r.description,
       r.stars,
       r.fork_count,
       r.html_url,
       r.languages,
       r.parent_id,
       r.has_issues,
       r.updated_at,
       i.indexed_at
FROM github_repos r
         LEFT JOIN github_repo_indexes i ON i.repo_id = r.id;

create view api.work_items
            (id, type, repo_id, number, payment_id, project_id, recipient_id, github_issue_id, github_pull_request_id,
             github_code_review_id)
as
SELECT w.id,
       w.type,
       w.repo_id,
       w.number,
       w.payment_id,
       w.project_id,
       w.recipient_id,
       CASE
           WHEN w.type = 'issue'::contribution_type THEN w.id::bigint
           ELSE NULL::bigint
           END AS github_issue_id,
       CASE
           WHEN w.type = 'pull_request'::contribution_type THEN w.id::bigint
           ELSE NULL::bigint
           END AS github_pull_request_id,
       CASE
           WHEN w.type = 'code_review'::contribution_type THEN w.id
           ELSE NULL::text
           END AS github_code_review_id
FROM work_items w;

create view api.github_issues
            (id, repo_id, number, created_at, author_id, status, title, html_url, closed_at, assignee_ids,
             comments_count)
as
SELECT github_issues.id,
       github_issues.repo_id,
       github_issues.number,
       github_issues.created_at,
       github_issues.author_id,
       upper(github_issues.status::text) AS status,
       github_issues.title,
       github_issues.html_url,
       github_issues.closed_at,
       github_issues.assignee_ids,
       github_issues.comments_count
FROM github_issues;

create view api.github_pull_request_reviews(id, pull_request_id, reviewer_id, status, outcome, submitted_at) as
SELECT github_pull_request_reviews.id,
       github_pull_request_reviews.pull_request_id,
       github_pull_request_reviews.reviewer_id,
       upper(github_pull_request_reviews.status::text) AS status,
       github_pull_request_reviews.outcome,
       github_pull_request_reviews.submitted_at
FROM github_pull_request_reviews;

create view api.contributions
            (id, github_user_id, details_id, type, repo_id, status, closed_at, created_at, project_id, github_issue_id,
             github_pull_request_id, github_code_review_id, ignored)
as
SELECT c.id,
       c.user_id                                       AS github_user_id,
       c.details_id,
       upper(c.type::text)                             AS type,
       c.repo_id,
       c.status,
       c.closed_at,
       c.created_at,
       pgr.project_id,
       CASE
           WHEN c.type = 'issue'::contribution_type THEN c.details_id::bigint
           ELSE NULL::bigint
           END                                         AS github_issue_id,
       CASE
           WHEN c.type = 'pull_request'::contribution_type THEN c.details_id::bigint
           ELSE NULL::bigint
           END                                         AS github_pull_request_id,
       CASE
           WHEN c.type = 'code_review'::contribution_type THEN c.details_id
           ELSE NULL::text
           END                                         AS github_code_review_id,
       (EXISTS (SELECT 1
                FROM ignored_contributions ic
                WHERE ic.contribution_id = c.id
                  AND ic.project_id = pgr.project_id)) AS ignored
FROM contributions c
         JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id;

create view api.github_pull_requests
            (id, repo_id, number, created_at, author_id, merged_at, status, title, html_url, closed_at, draft,
             ci_checks)
as
SELECT pr.id,
       pr.repo_id,
       pr.number,
       pr.created_at,
       pr.author_id,
       pr.merged_at,
       upper(pr.status::text) AS status,
       pr.title,
       pr.html_url,
       pr.closed_at,
       pr.draft,
       pr.ci_checks
FROM github_pull_requests pr;

create view api.closing_issues(github_pull_request_id, github_issue_id) as
SELECT closing_issues.github_pull_request_id,
       closing_issues.github_issue_id
FROM closing_issues;

create view api.closed_by_pull_requests(github_pull_request_id, github_issue_id) as
SELECT closing_issues.github_pull_request_id,
       closing_issues.github_issue_id
FROM closing_issues;

create view api.projects
            (id, key, name, logo_url, short_description, long_description, more_info_link, rank, hiring, visibility,
             usd_budget_id, eth_budget_id, op_budget_id, apt_budget_id, stark_budget_id)
as
SELECT p.id,
       pd.key,
       pd.name,
       pd.logo_url,
       pd.short_description,
       pd.long_description,
       pd.telegram_link AS more_info_link,
       pd.rank,
       pd.hiring,
       pd.visibility,
       usd_budget.id    AS usd_budget_id,
       eth_budget.id    AS eth_budget_id,
       op_budget.id     AS op_budget_id,
       apt_budget.id    AS apt_budget_id,
       stark_budget.id  AS stark_budget_id
FROM projects p
         JOIN project_details pd ON pd.project_id = p.id
         LEFT JOIN budgets usd_budget ON (usd_budget.id IN (SELECT projects_budgets.budget_id
                                                            FROM projects_budgets
                                                            WHERE projects_budgets.project_id = p.id)) AND
                                         usd_budget.currency = 'usd'::currency
         LEFT JOIN budgets eth_budget ON (eth_budget.id IN (SELECT projects_budgets.budget_id
                                                            FROM projects_budgets
                                                            WHERE projects_budgets.project_id = p.id)) AND
                                         eth_budget.currency = 'eth'::currency
         LEFT JOIN budgets op_budget ON (op_budget.id IN (SELECT projects_budgets.budget_id
                                                          FROM projects_budgets
                                                          WHERE projects_budgets.project_id = p.id)) AND
                                        op_budget.currency = 'op'::currency
         LEFT JOIN budgets apt_budget ON (apt_budget.id IN (SELECT projects_budgets.budget_id
                                                            FROM projects_budgets
                                                            WHERE projects_budgets.project_id = p.id)) AND
                                         apt_budget.currency = 'apt'::currency
         LEFT JOIN budgets stark_budget ON (stark_budget.id IN (SELECT projects_budgets.budget_id
                                                                FROM projects_budgets
                                                                WHERE projects_budgets.project_id = p.id)) AND
                                           stark_budget.currency = 'stark'::currency;

create view api.user_payout_info
            (user_id, is_company, lastname, firstname, company_name, company_identification_number, address, post_code,
             city, country, usd_preferred_method, bic, iban, eth_wallet, optimism_wallet, aptos_wallet, starknet_wallet,
             are_payout_settings_valid)
as
WITH info AS (SELECT upi.user_id,
                     upi.identity ? 'Company'::text                                 AS is_company,
                     COALESCE(upi.identity #>> '{Person,lastname}'::text[],
                              upi.identity #>> '{Company,owner,lastname}'::text[])  AS lastname,
                     COALESCE(upi.identity #>> '{Person,firstname}'::text[],
                              upi.identity #>> '{Company,owner,firstname}'::text[]) AS firstname,
                     upi.identity #>> '{Company,name}'::text[]                      AS company_name,
                     upi.identity #>> '{Company,identification_number}'::text[]     AS company_identification_number,
                     upi.location ->> 'address'::text                               AS address,
                     upi.location ->> 'post_code'::text                             AS post_code,
                     upi.location ->> 'city'::text                                  AS city,
                     upi.location ->> 'country'::text                               AS country,
                     upper(upi.usd_preferred_method::text)                          AS usd_preferred_method,
                     ba.bic,
                     ba.iban,
                     COALESCE(eth_name.address, eth_address.address)                AS eth_wallet,
                     optimism_wallet.address                                        AS optimism_wallet,
                     aptos_address.address                                          AS aptos_wallet,
                     starknet_address.address                                       AS starknet_wallet
              FROM user_payout_info upi
                       LEFT JOIN bank_accounts ba ON ba.user_id = upi.user_id
                       LEFT JOIN wallets eth_name
                                 ON eth_name.user_id = upi.user_id AND eth_name.network = 'ethereum'::network AND
                                    eth_name.type = 'name'::wallet_type
                       LEFT JOIN wallets eth_address
                                 ON eth_address.user_id = upi.user_id AND eth_address.network = 'ethereum'::network AND
                                    eth_address.type = 'address'::wallet_type
                       LEFT JOIN wallets aptos_address
                                 ON aptos_address.user_id = upi.user_id AND aptos_address.network = 'aptos'::network AND
                                    aptos_address.type = 'address'::wallet_type
                       LEFT JOIN wallets optimism_wallet ON optimism_wallet.user_id = upi.user_id AND
                                                            optimism_wallet.network = 'optimism'::network AND
                                                            optimism_wallet.type = 'address'::wallet_type
                       LEFT JOIN wallets starknet_address ON starknet_address.user_id = upi.user_id AND
                                                             starknet_address.network = 'starknet'::network AND
                                                             starknet_address.type = 'address'::wallet_type),
     pending_payments AS (SELECT payment_requests.recipient_id,
                                 count(*) AS total
                          FROM payment_requests
                          WHERE NOT (EXISTS (SELECT 1
                                             FROM payments
                                             WHERE payments.request_id = payment_requests.id))
                          GROUP BY payment_requests.recipient_id)
SELECT info.user_id,
       info.is_company,
       info.lastname,
       info.firstname,
       info.company_name,
       info.company_identification_number,
       info.address,
       info.post_code,
       info.city,
       info.country,
       info.usd_preferred_method,
       info.bic,
       info.iban,
       info.eth_wallet,
       info.optimism_wallet,
       info.aptos_wallet,
       info.starknet_wallet,
       pp.total = 0 OR info.lastname IS NOT NULL AND info.firstname IS NOT NULL AND info.address IS NOT NULL AND
                       info.post_code IS NOT NULL AND info.city IS NOT NULL AND info.country IS NOT NULL AND
                       (NOT info.is_company OR
                        info.company_name IS NOT NULL AND info.company_identification_number IS NOT NULL) AND
                       (info.usd_preferred_method = 'FIAT'::text AND info.bic IS NOT NULL AND info.iban IS NOT NULL OR
                        info.eth_wallet IS NOT NULL) AS are_payout_settings_valid
FROM info
         JOIN registered_users ru ON ru.id = info.user_id
         LEFT JOIN pending_payments pp ON pp.recipient_id = ru.github_user_id;

create view api.payment_stats (github_user_id, project_id, money_granted, currency, money_granted_usd) as
SELECT p.recipient_id          AS github_user_id,
       p.project_id,
       sum(p.amount)           AS money_granted,
       upper(p.currency::text) AS currency,
       sum(p.amount) *
       CASE
           WHEN p.currency = 'usd'::currency THEN 1::numeric
           ELSE q.price
           END                 AS money_granted_usd
FROM payment_requests p
         LEFT JOIN crypto_usd_quotes q ON q.currency = p.currency
GROUP BY p.recipient_id, p.project_id, p.currency, q.price;

create view api.budgets
            (id, currency, initial_amount, initial_amount_usd, remaining_amount, remaining_amount_usd, spent_amount,
             spent_amount_usd)
as
WITH budgets_with_rates AS (SELECT b_1.id,
                                   b_1.currency,
                                   b_1.initial_amount,
                                   b_1.remaining_amount,
                                   b_1.initial_amount - b_1.remaining_amount AS spent_amount,
                                   CASE
                                       WHEN b_1.currency = 'usd'::currency THEN 1::numeric
                                       ELSE q.price
                                       END                                   AS usd_rate
                            FROM budgets b_1
                                     LEFT JOIN crypto_usd_quotes q ON b_1.currency = q.currency)
SELECT b.id,
       upper(b.currency::text)         AS currency,
       b.initial_amount,
       b.initial_amount * b.usd_rate   AS initial_amount_usd,
       b.remaining_amount,
       b.remaining_amount * b.usd_rate AS remaining_amount_usd,
       b.spent_amount,
       b.spent_amount * b.usd_rate     AS spent_amount_usd
FROM budgets_with_rates b;

create view api.payment_requests
            (id, requestor_id, recipient_id, amount, requested_at, invoice_received_at, hours_worked, project_id,
             currency, amount_usd)
as
SELECT p.id,
       p.requestor_id,
       p.recipient_id,
       p.amount,
       p.requested_at,
       p.invoice_received_at,
       p.hours_worked,
       p.project_id,
       p.currency,
       p.amount *
       CASE
           WHEN p.currency = 'usd'::currency THEN 1::numeric
           ELSE q.price
           END AS amount_usd
FROM payment_requests p
         LEFT JOIN crypto_usd_quotes q ON q.currency = p.currency;

create function public.diesel_manage_updated_at(_tbl regclass) returns void
    language plpgsql
as
$$
BEGIN
    EXECUTE format('CREATE TRIGGER set_updated_at BEFORE UPDATE ON %s
                    FOR EACH ROW EXECUTE PROCEDURE diesel_set_updated_at()', _tbl);
END;
$$;

create function public.diesel_set_updated_at() returns trigger
    language plpgsql
as
$$
BEGIN
    IF (
            NEW IS DISTINCT FROM OLD AND
            NEW.updated_at IS NOT DISTINCT FROM OLD.updated_at
        ) THEN
        NEW.updated_at := current_timestamp;
    END IF;
    RETURN NEW;
END;
$$;

create function public.insert_github_user_indexes_from_auth_users() returns trigger
    language plpgsql
as
$$
BEGIN
    -- Insert or update a row into table github_user_indexes when a row is inserted in auth.user_providers
    INSERT INTO public.github_user_indexes (user_id)
    VALUES (NEW.provider_user_id::bigint)
    ON CONFLICT (user_id) DO NOTHING;

    RETURN NEW;
END;
$$;

create function public.replicate_users_changes() returns trigger
    language plpgsql
as
$$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO auth_users (id, email, last_seen, login_at_signup, avatar_url_at_signup, created_at)
        VALUES (NEW.id, NEW.email, NEW.last_seen, NEW.display_name, NEW.avatar_url, NEW.created_at);
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE
            auth_users
        SET email                = NEW.email,
            last_seen            = NEW.last_seen,
            login_at_signup      = NEW.display_name,
            avatar_url_at_signup = NEW.avatar_url,
            created_at           = NEW.created_at
        WHERE id = NEW.id;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE
        FROM auth_users
        WHERE id = OLD.id;
    END IF;
    RETURN NULL;
END;
$$;

create function public.replicate_user_providers_changes() returns trigger
    language plpgsql
as
$$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.provider_id = 'github' THEN
        UPDATE
            auth_users
        SET github_user_id = NEW.provider_user_id::bigint
        WHERE id = NEW.user_id;
    ELSIF TG_OP = 'UPDATE' AND NEW.provider_id = 'github' THEN
        UPDATE
            auth_users
        SET github_user_id = NEW.provider_user_id::bigint
        WHERE id = NEW.user_id;
    ELSIF TG_OP = 'DELETE' AND OLD.provider_id = 'github' THEN
        UPDATE
            auth_users
        SET github_user_id = NULL
        WHERE id = OLD.user_id;
    END IF;
    RETURN NULL;
END;
$$;



