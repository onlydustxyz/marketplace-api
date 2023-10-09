-- This migration is needed so that hasura auth can populate its auth schema

-- It is duplicated in migrations/2022-11-15-145459_create-hasura-auth-schema/up.sql
-- so that it runs also on deploy in Heroku

-- auth schema
CREATE SCHEMA IF NOT EXISTS auth;
-- https://github.com/hasura/graphql-engine/issues/3657
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;
CREATE OR REPLACE FUNCTION public.set_current_timestamp_updated_at() RETURNS trigger LANGUAGE plpgsql AS $$
declare _new record;
begin _new := new;
_new."updated_at" = now();
return _new;
end;
$$;

create type public.project_visibility as enum ('public', 'private');


create type public.contact_channel as enum ('email', 'telegram', 'twitter', 'discord', 'linkedin', 'whatsapp');


create type public.allocated_time as enum ('none', 'lt1day', '1to3days', 'gt3days');


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

create table public.event_deduplications
(
    deduplication_id text              not null
        primary key,
    event_index      integer default 0 not null
);


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

create table public.projects
(
    id uuid not null
        primary key
);


create table public.project_leads
(
    project_id  uuid                    not null,
    user_id     uuid                    not null,
    assigned_at timestamp default now() not null,
    primary key (project_id, user_id)
);


create unique index project_leads_user_id_project_id_idx
    on public.project_leads (user_id, project_id);

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

create table public.project_details
(
    project_id        uuid                                                                                        not null
        primary key,
    telegram_link     text,
    logo_url          text,
    name              text    default ''::text                                                                    not null,
    short_description text    default ''::text                                                                    not null,
    long_description  text    default ''::text                                                                    not null,
    hiring            boolean default false                                                                       not null,
    rank              integer default 0                                                                           not null,
    visibility        project_visibility                                                                          not null,
    key               text generated always as (lower(replace(
            regexp_replace(name, '[^a-zA-Z0-9_\-\ ]+'::text, ''::text, 'g'::text), ' '::text,
            '-'::text))) stored                                                                                   not null
);


create index project_details_rank_idx
    on public.project_details (rank desc);

create index project_details_name_idx
    on public.project_details (name);


create unique index project_details_key
    on public.project_details (key);

create table public.user_payout_info
(
    user_id              uuid not null
        constraint user_info_pkey
            primary key,
    identity             jsonb,
    location             jsonb,
    usd_preferred_method preferred_method
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


create table public.project_github_repos
(
    project_id     uuid   not null,
    github_repo_id bigint not null,
    primary key (project_id, github_repo_id)
);


create unique index project_github_repos_github_repo_id_project_id_idx
    on public.project_github_repos (github_repo_id, project_id);

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

create table public.onboardings
(
    user_id                              uuid not null
        constraint terms_and_conditions_acceptances_pkey
            primary key,
    terms_and_conditions_acceptance_date timestamp,
    profile_wizard_display_date          timestamp
);


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


create table public.closing_issues
(
    github_issue_id        bigint not null,
    github_pull_request_id bigint not null,
    primary key (github_issue_id, github_pull_request_id)
);


create table public.crypto_usd_quotes
(
    currency   currency  not null
        primary key,
    price      numeric   not null,
    updated_at timestamp not null
);
