create type public.contribution_status as enum ('in_progress', 'complete', 'canceled');
create type public.preferred_method as enum ('fiat', 'crypto');

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



create table public.project_leads
(
    project_id  uuid                    not null,
    user_id     uuid                    not null,
    assigned_at timestamp default now() not null,
    primary key (project_id, user_id)
);

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


create table public.project_github_repos
(
    project_id     uuid   not null,
    github_repo_id bigint not null,
    primary key (project_id, github_repo_id)
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



create table public.projects_contributors
(
    project_id     uuid   not null,
    github_user_id bigint not null,
    primary key (project_id, github_user_id)
);


create table public.technologies
(
    technology text not null
        primary key
);


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



create table public.projects_rewarded_users
(
    project_id     uuid    not null,
    github_user_id bigint  not null,
    reward_count   integer not null,
    primary key (project_id, github_user_id)
);


create table public.projects_pending_contributors
(
    project_id     uuid   not null,
    github_user_id bigint not null,
    primary key (project_id, github_user_id)
);


create table public.ignored_contributions
(
    project_id      uuid not null,
    contribution_id text not null,
    primary key (project_id, contribution_id)
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


--  ###################### Indexer ######################

create type public.github_pull_request_status as enum ('open', 'closed', 'merged');
create type public.github_issue_status as enum ('open', 'completed', 'cancelled');
create type public.github_ci_checks as enum ('passed', 'failed');
create type public.github_code_review_status as enum ('pending', 'completed');
create type public.github_code_review_outcome as enum ('change_requested', 'approved');



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


create table public.github_user_indexes
(
    user_id                   bigint not null
        primary key,
    user_indexer_state        jsonb,
    contributor_indexer_state jsonb,
    indexed_at                timestamp
);

