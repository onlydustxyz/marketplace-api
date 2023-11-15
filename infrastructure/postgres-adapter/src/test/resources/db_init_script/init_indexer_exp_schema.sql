create schema indexer_exp;

create type indexer_exp.contribution_status as enum ('IN_PROGRESS', 'COMPLETED', 'CANCELLED');

create type indexer_exp.contribution_type as enum ('PULL_REQUEST', 'ISSUE', 'CODE_REVIEW');

create type indexer_exp.github_account_type as enum ('USER', 'ORGANIZATION', 'BOT');

create type indexer_exp.github_code_review_state as enum ('PENDING', 'COMMENTED', 'APPROVED', 'CHANGES_REQUESTED', 'DISMISSED');

create type indexer_exp.github_issue_status as enum ('OPEN', 'COMPLETED', 'CANCELLED');

create type indexer_exp.github_pull_request_status as enum ('OPEN', 'CLOSED', 'MERGED');

create table indexer_exp.github_accounts
(
    id              bigint                          not null
        primary key,
    login           text                            not null,
    type            indexer_exp.github_account_type not null,
    html_url        text                            not null,
    avatar_url      text,
    name            text,
    tech_created_at timestamp default now()         not null,
    tech_updated_at timestamp default now()         not null
);


create table indexer_exp.github_app_installations
(
    id              bigint                  not null
        primary key,
    account_id      bigint                  not null
        references indexer_exp.github_accounts,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null
);

create table indexer_exp.github_repos
(
    id              bigint                        not null
        primary key,
    owner_id        bigint                        not null
        references indexer_exp.github_accounts,
    name            text                          not null,
    html_url        text                          not null,
    updated_at      timestamp                     not null,
    description     text,
    stars_count     bigint                        not null,
    forks_count     bigint                        not null,
    languages       jsonb     default '{}'::jsonb not null,
    has_issues      boolean   default true        not null,
    parent_id       bigint
        references indexer_exp.github_repos,
    tech_created_at timestamp default now()       not null,
    tech_updated_at timestamp default now()       not null
);

create table indexer_exp.authorized_github_repos
(
    repo_id         bigint not null
        references indexer_exp.github_repos,
    installation_id bigint not null
        references indexer_exp.github_app_installations,
    primary key (repo_id, installation_id)
);

create table indexer_exp.github_issues
(
    id              bigint                          not null
        primary key,
    repo_id         bigint                          not null
        references indexer_exp.github_repos,
    number          bigint                          not null,
    title           text                            not null,
    status          indexer_exp.github_issue_status not null,
    created_at      timestamp                       not null,
    closed_at       timestamp,
    author_id       bigint                          not null
        references indexer_exp.github_accounts,
    html_url        text                            not null,
    body            text,
    comments_count  integer                         not null,
    tech_created_at timestamp default now()         not null,
    tech_updated_at timestamp default now()         not null
);


create table indexer_exp.github_issues_assignees
(
    issue_id bigint not null
        references indexer_exp.github_issues,
    user_id  bigint not null
        references indexer_exp.github_accounts,
    primary key (issue_id, user_id)
);

create table indexer_exp.github_pull_requests
(
    id              bigint                                 not null
        primary key,
    repo_id         bigint                                 not null
        references indexer_exp.github_repos,
    number          bigint                                 not null,
    title           text                                   not null,
    status          indexer_exp.github_pull_request_status not null,
    created_at      timestamp                              not null,
    closed_at       timestamp,
    merged_at       timestamp,
    author_id       bigint                                 not null
        references indexer_exp.github_accounts,
    html_url        text                                   not null,
    body            text,
    comments_count  integer                                not null,
    tech_created_at timestamp default now()                not null,
    tech_updated_at timestamp default now()                not null
);

create table indexer_exp.github_code_reviews
(
    id              text                                 not null
        primary key,
    pull_request_id bigint                               not null
        references indexer_exp.github_pull_requests,
    author_id       bigint                               not null
        references indexer_exp.github_accounts,
    state           indexer_exp.github_code_review_state not null,
    requested_at    timestamp                            not null,
    submitted_at    timestamp,
    tech_created_at timestamp default now()              not null,
    tech_updated_at timestamp default now()              not null
);

create table indexer_exp.contributions
(
    id              text                            not null
        primary key,
    repo_id         bigint                          not null
        references indexer_exp.github_repos,
    contributor_id  bigint                          not null
        references indexer_exp.github_accounts,
    type            indexer_exp.contribution_type   not null,
    status          indexer_exp.contribution_status not null,
    pull_request_id bigint
        references indexer_exp.github_pull_requests,
    issue_id        bigint
        references indexer_exp.github_issues,
    code_review_id  text
        references indexer_exp.github_code_reviews,
    created_at      timestamp                       not null,
    completed_at    timestamp,
    tech_created_at timestamp default now()         not null,
    tech_updated_at timestamp default now()         not null,
    constraint contributions_contributor_id_repo_id_pull_request_id_issue__key
        unique (contributor_id, repo_id, pull_request_id, issue_id),
    constraint contributions_check
        check (((type = 'PULL_REQUEST'::indexer_exp.contribution_type) AND (pull_request_id IS NOT NULL) AND
                (issue_id IS NULL) AND (code_review_id IS NULL)) OR
               ((type = 'ISSUE'::indexer_exp.contribution_type) AND (pull_request_id IS NULL) AND
                (issue_id IS NOT NULL) AND (code_review_id IS NULL)) OR
               ((type = 'CODE_REVIEW'::indexer_exp.contribution_type) AND (pull_request_id IS NULL) AND
                (issue_id IS NULL) AND (code_review_id IS NOT NULL)))
);

create table indexer_exp.github_pull_requests_closing_issues
(
    pull_request_id bigint not null
        references indexer_exp.github_pull_requests,
    issue_id        bigint not null
        references indexer_exp.github_issues,
    primary key (pull_request_id, issue_id)
);


create index github_pull_requests_closing_issues_issue_id_idx
    on indexer_exp.github_pull_requests_closing_issues (issue_id, pull_request_id);

create table indexer_exp.repos_contributors
(
    repo_id        bigint not null
        references indexer_exp.github_repos,
    contributor_id bigint not null
        references indexer_exp.github_accounts,
    primary key (repo_id, contributor_id)
);


