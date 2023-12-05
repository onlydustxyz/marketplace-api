create schema indexer_exp;

create type indexer_exp.github_account_type as enum ('USER', 'ORGANIZATION', 'BOT');

create type indexer_exp.github_issue_status as enum ('OPEN', 'COMPLETED', 'CANCELLED');

create type indexer_exp.github_pull_request_status as enum ('OPEN', 'CLOSED', 'MERGED', 'DRAFT');

create type indexer_exp.github_code_review_state as enum ('PENDING', 'COMMENTED', 'APPROVED', 'CHANGES_REQUESTED', 'DISMISSED');

create type indexer_exp.contribution_type as enum ('PULL_REQUEST', 'ISSUE', 'CODE_REVIEW');

create type indexer_exp.contribution_status as enum ('IN_PROGRESS', 'COMPLETED', 'CANCELLED');

create type indexer_exp.github_pull_request_review_state as enum ('PENDING_REVIEWER', 'UNDER_REVIEW', 'APPROVED', 'CHANGES_REQUESTED');

create type indexer_exp.github_repo_visibility as enum ('PUBLIC', 'PRIVATE');

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
    tech_updated_at timestamp default now()         not null,
    bio             text,
    location        text,
    website         text,
    twitter         text,
    linkedin        text,
    telegram        text
);

create trigger indexer_exp_github_accounts_set_tech_updated_at
    before update
    on indexer_exp.github_accounts
    for each row
execute procedure public.set_tech_updated_at();

create table indexer_exp.github_repos
(
    id              bigint                             not null
        primary key,
    owner_id        bigint                             not null
        references indexer_exp.github_accounts,
    name            text                               not null,
    html_url        text                               not null,
    updated_at      timestamp,
    description     text,
    stars_count     bigint,
    forks_count     bigint,
    languages       jsonb     default '{}'::jsonb      not null,
    has_issues      boolean,
    parent_id       bigint
        references indexer_exp.github_repos,
    tech_created_at timestamp default now()            not null,
    tech_updated_at timestamp default now()            not null,
    owner_login     text                               not null,
    visibility      indexer_exp.github_repo_visibility not null
);

create unique index github_repos_owner_login_name_idx
    on indexer_exp.github_repos (owner_login, name);

create trigger indexer_exp_github_repos_set_tech_updated_at
    before update
    on indexer_exp.github_repos
    for each row
execute procedure public.set_tech_updated_at();

create table indexer_exp.github_issues
(
    id                bigint                          not null
        primary key,
    repo_id           bigint                          not null
        references indexer_exp.github_repos,
    number            bigint                          not null,
    title             text                            not null,
    status            indexer_exp.github_issue_status not null,
    created_at        timestamp                       not null,
    closed_at         timestamp,
    author_id         bigint                          not null
        references indexer_exp.github_accounts,
    html_url          text                            not null,
    body              text,
    comments_count    integer                         not null,
    tech_created_at   timestamp default now()         not null,
    tech_updated_at   timestamp default now()         not null,
    repo_owner_login  text                            not null,
    repo_name         text                            not null,
    repo_html_url     text                            not null,
    author_login      text                            not null,
    author_html_url   text                            not null,
    author_avatar_url text                            not null
);

create trigger indexer_exp_github_issues_set_tech_updated_at
    before update
    on indexer_exp.github_issues
    for each row
execute procedure public.set_tech_updated_at();

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
    id                bigint                                       not null
        primary key,
    repo_id           bigint                                       not null
        references indexer_exp.github_repos,
    number            bigint                                       not null,
    title             text                                         not null,
    status            indexer_exp.github_pull_request_status       not null,
    created_at        timestamp                                    not null,
    closed_at         timestamp,
    merged_at         timestamp,
    author_id         bigint                                       not null
        references indexer_exp.github_accounts,
    html_url          text                                         not null,
    body              text,
    comments_count    integer                                      not null,
    tech_created_at   timestamp default now()                      not null,
    tech_updated_at   timestamp default now()                      not null,
    draft             boolean   default false                      not null,
    repo_owner_login  text                                         not null,
    repo_name         text                                         not null,
    repo_html_url     text                                         not null,
    author_login      text                                         not null,
    author_html_url   text                                         not null,
    author_avatar_url text                                         not null,
    review_state      indexer_exp.github_pull_request_review_state not null,
    commit_count      integer                                      not null
);

create trigger indexer_exp_github_pull_requests_set_tech_updated_at
    before update
    on indexer_exp.github_pull_requests
    for each row
execute procedure public.set_tech_updated_at();

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

create table indexer_exp.github_code_reviews
(
    id                text                                 not null
        primary key,
    pull_request_id   bigint                               not null
        references indexer_exp.github_pull_requests,
    author_id         bigint                               not null
        references indexer_exp.github_accounts,
    state             indexer_exp.github_code_review_state not null,
    requested_at      timestamp                            not null,
    submitted_at      timestamp,
    tech_created_at   timestamp default now()              not null,
    tech_updated_at   timestamp default now()              not null,
    number            bigint,
    title             text,
    html_url          text,
    body              text,
    comments_count    integer,
    repo_owner_login  text,
    repo_name         text,
    repo_id           bigint                               not null
        references indexer_exp.github_repos,
    repo_html_url     text                                 not null,
    author_login      text                                 not null,
    author_html_url   text                                 not null,
    author_avatar_url text                                 not null
);

create trigger indexer_exp_github_code_reviews_set_tech_updated_at
    before update
    on indexer_exp.github_code_reviews
    for each row
execute procedure public.set_tech_updated_at();

create table indexer_exp.contributions
(
    id                       text                            not null
        primary key,
    repo_id                  bigint                          not null
        references indexer_exp.github_repos,
    contributor_id           bigint                          not null
        references indexer_exp.github_accounts,
    type                     indexer_exp.contribution_type   not null,
    status                   indexer_exp.contribution_status not null,
    pull_request_id          bigint
        references indexer_exp.github_pull_requests,
    issue_id                 bigint
        references indexer_exp.github_issues,
    code_review_id           text
        references indexer_exp.github_code_reviews,
    created_at               timestamp                       not null,
    completed_at             timestamp,
    tech_created_at          timestamp default now()         not null,
    tech_updated_at          timestamp default now()         not null,
    github_number            bigint                          not null,
    github_status            text                            not null,
    github_title             text                            not null,
    github_html_url          text                            not null,
    github_body              text,
    github_comments_count    integer                         not null,
    repo_owner_login         text                            not null,
    repo_name                text                            not null,
    repo_html_url            text                            not null,
    github_author_id         bigint                          not null
        references indexer_exp.github_accounts,
    github_author_login      text                            not null,
    github_author_html_url   text                            not null,
    github_author_avatar_url text                            not null,
    contributor_login        text                            not null,
    contributor_html_url     text                            not null,
    contributor_avatar_url   text                            not null,
    pr_review_state          indexer_exp.github_pull_request_review_state,
    constraint contributions_unique_constraint
        unique (contributor_id, repo_id, pull_request_id, issue_id, code_review_id),
    constraint contributions_check
        check (((type = 'PULL_REQUEST'::indexer_exp.contribution_type) AND (pull_request_id IS NOT NULL) AND
                (issue_id IS NULL) AND (code_review_id IS NULL)) OR
               ((type = 'ISSUE'::indexer_exp.contribution_type) AND (pull_request_id IS NULL) AND
                (issue_id IS NOT NULL) AND (code_review_id IS NULL)) OR
               ((type = 'CODE_REVIEW'::indexer_exp.contribution_type) AND (pull_request_id IS NULL) AND
                (issue_id IS NULL) AND (code_review_id IS NOT NULL)))
);

create trigger indexer_exp_contributions_set_tech_updated_at
    before update
    on indexer_exp.contributions
    for each row
execute procedure public.set_tech_updated_at();

create table indexer_exp.github_app_installations
(
    id              bigint                  not null
        primary key,
    account_id      bigint                  not null
        references indexer_exp.github_accounts,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null,
    suspended_at    timestamp
);

create unique index github_app_installations_account_id_idx
    on indexer_exp.github_app_installations (account_id);

create trigger indexer_exp_github_app_installations_set_tech_updated_at
    before update
    on indexer_exp.github_app_installations
    for each row
execute procedure public.set_tech_updated_at();

create table indexer_exp.authorized_github_repos
(
    repo_id         bigint not null
        references indexer_exp.github_repos,
    installation_id bigint not null
        references indexer_exp.github_app_installations,
    primary key (repo_id, installation_id)
);

create table indexer_exp.repos_contributors
(
    repo_id                      bigint  not null
        references indexer_exp.github_repos,
    contributor_id               bigint  not null
        references indexer_exp.github_accounts,
    completed_contribution_count integer not null,
    total_contribution_count     integer not null,
    primary key (repo_id, contributor_id)
);

create table indexer_exp.github_pull_request_commit_counts
(
    pull_request_id bigint  not null
        references indexer_exp.github_pull_requests
            deferrable initially deferred,
    author_id       bigint  not null
        references indexer_exp.github_accounts
            deferrable initially deferred,
    commit_count    integer not null,
    primary key (pull_request_id, author_id)
);

CREATE TABLE indexer_exp.github_repos_stats
(
    id              BIGINT PRIMARY KEY REFERENCES indexer_exp.github_repos (id),
    last_indexed_at TIMESTAMP
);

