create schema indexer_exp;

create type indexer_exp.contribution_status as enum ('IN_PROGRESS', 'COMPLETED', 'CANCELLED');

create type indexer_exp.contribution_type as enum ('PULL_REQUEST', 'ISSUE', 'CODE_REVIEW');

create type indexer_exp.github_account_type as enum ('USER', 'ORGANIZATION', 'BOT');

create type indexer_exp.github_code_review_state as enum ('PENDING', 'COMMENTED', 'APPROVED', 'CHANGES_REQUESTED', 'DISMISSED');

create type indexer_exp.github_issue_status as enum ('OPEN', 'COMPLETED', 'CANCELLED');

create type indexer_exp.github_pull_request_review_state as enum ('PENDING_REVIEWER', 'UNDER_REVIEW', 'APPROVED', 'CHANGES_REQUESTED');

create type indexer_exp.github_pull_request_status as enum ('OPEN', 'CLOSED', 'MERGED', 'DRAFT');

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
    telegram        text,
    created_at      timestamp
);

create unique index github_accounts_login_uidx
    on indexer_exp.github_accounts (login);


create table indexer_exp.github_app_installations
(
    id              bigint                  not null
        primary key,
    account_id      bigint                  not null
        references indexer_exp.github_accounts,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null,
    suspended_at    timestamp,
    permissions     text[]                  not null
);

create unique index github_app_installations_account_id_idx
    on indexer_exp.github_app_installations (account_id);

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
    has_issues      boolean,
    parent_id       bigint
        references indexer_exp.github_repos,
    tech_created_at timestamp default now()            not null,
    tech_updated_at timestamp default now()            not null,
    owner_login     text                               not null,
    visibility      indexer_exp.github_repo_visibility not null,
    deleted_at      timestamp
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
    author_avatar_url text                            not null,
    updated_at        timestamp with time zone        not null,
    contribution_uuid uuid                            not null
);

create unique index github_issues_id_as_text_index
    on indexer_exp.github_issues ((id::text));

create index github_issues_repo_id_index
    on indexer_exp.github_issues (repo_id);

create index github_issues_status_index
    on indexer_exp.github_issues (status);

create unique index github_issues_contribution_uuid_index
    on indexer_exp.github_issues (contribution_uuid);

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
    id                   bigint                                       not null
        primary key,
    repo_id              bigint                                       not null
        references indexer_exp.github_repos,
    number               bigint                                       not null,
    title                text                                         not null,
    status               indexer_exp.github_pull_request_status       not null,
    created_at           timestamp                                    not null,
    closed_at            timestamp,
    merged_at            timestamp,
    author_id            bigint                                       not null
        references indexer_exp.github_accounts,
    html_url             text                                         not null,
    body                 text,
    comments_count       integer                                      not null,
    tech_created_at      timestamp default now()                      not null,
    tech_updated_at      timestamp default now()                      not null,
    draft                boolean   default false                      not null,
    repo_owner_login     text                                         not null,
    repo_name            text                                         not null,
    repo_html_url        text                                         not null,
    author_login         text                                         not null,
    author_html_url      text                                         not null,
    author_avatar_url    text                                         not null,
    review_state         indexer_exp.github_pull_request_review_state not null,
    commit_count         integer                                      not null,
    main_file_extensions text[],
    updated_at           timestamp with time zone                     not null,
    contribution_uuid    uuid                                         not null
);

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
    author_avatar_url text                                 not null,
    contribution_uuid uuid                                 not null
);

create table indexer_exp.contributions
(
    id                       text                            not null
        primary key,
    repo_id                  bigint                          not null
        references indexer_exp.github_repos,
    contributor_id           bigint
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
    contributor_login        text,
    contributor_html_url     text,
    contributor_avatar_url   text,
    pr_review_state          indexer_exp.github_pull_request_review_state,
    main_file_extensions     text[],
    updated_at               timestamp with time zone        not null,
    contribution_uuid        uuid                            not null,
    constraint contributions_unique_constraint
        unique (contributor_id, repo_id, pull_request_id, issue_id, code_review_id),
    constraint contributions_check
        check (((type = 'PULL_REQUEST'::indexer_exp.contribution_type) AND (pull_request_id IS NOT NULL) AND (issue_id IS NULL) AND (code_review_id IS NULL)) OR
               ((type = 'ISSUE'::indexer_exp.contribution_type) AND (pull_request_id IS NULL) AND (issue_id IS NOT NULL) AND (code_review_id IS NULL)) OR
               ((type = 'CODE_REVIEW'::indexer_exp.contribution_type) AND (pull_request_id IS NULL) AND (issue_id IS NULL) AND (code_review_id IS NOT NULL)))
);

create index contributions_pull_request_id_as_text_index
    on indexer_exp.contributions ((pull_request_id::text));

create index contributions_code_review_id_as_text_index
    on indexer_exp.contributions (code_review_id);

create index contributions_issue_id_as_text_index
    on indexer_exp.contributions ((issue_id::text));

create index contributions_contributor_id_index
    on indexer_exp.contributions (contributor_id);

create index contributions_repo_id_index
    on indexer_exp.contributions (repo_id);

create index contributions_status_as_text_index
    on indexer_exp.contributions (status);

create index contributions_type_as_text_index
    on indexer_exp.contributions (type);

create index contributions_completed_at_index
    on indexer_exp.contributions (completed_at);

create index contributions_created_at_index
    on indexer_exp.contributions (created_at);

create index contributions_completed_at_created_at_idx
    on indexer_exp.contributions (COALESCE(completed_at, created_at));

create index contributions_completed_at_created_at_desc_idx
    on indexer_exp.contributions (COALESCE(completed_at, created_at) desc);

create index indexer_exp_contributions_contributor_id_created_at_idx
    on indexer_exp.contributions (contributor_id, created_at);

create index contributions_contribution_uuid_index
    on indexer_exp.contributions (contribution_uuid);

create index github_code_reviews_pull_request_id_index
    on indexer_exp.github_code_reviews (pull_request_id);

create unique index github_code_reviews_contribution_uuid_index
    on indexer_exp.github_code_reviews (contribution_uuid);

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

create unique index github_pull_requests_id_as_text_index
    on indexer_exp.github_pull_requests ((id::text));

create unique index github_pull_requests_contribution_uuid_index
    on indexer_exp.github_pull_requests (contribution_uuid);

create table indexer_exp.github_pull_requests_closing_issues
(
    pull_request_id bigint not null
        references indexer_exp.github_pull_requests,
    issue_id        bigint not null
        references indexer_exp.github_issues,
    primary key (pull_request_id, issue_id)
);

create unique index github_pull_requests_closing_issues_issue_id_idx
    on indexer_exp.github_pull_requests_closing_issues (issue_id, pull_request_id);

create table indexer_exp.github_repo_languages
(
    repo_id         bigint                  not null
        references indexer_exp.github_repos,
    language        text                    not null,
    line_count      bigint                  not null,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null,
    primary key (repo_id, language)
);

create index github_repos_owner_login_name_idx
    on indexer_exp.github_repos (owner_login, name);

create index github_repos_id_visibility_idx
    on indexer_exp.github_repos (id, visibility);

create table indexer_exp.github_repos_stats
(
    id              bigint not null
        primary key
        references indexer_exp.github_repos,
    last_indexed_at timestamp
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

create table indexer_exp.github_labels
(
    id              bigint                  not null
        primary key,
    name            text                    not null,
    description     text,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null
);

create table indexer_exp.github_issues_labels
(
    issue_id        bigint                  not null
        references indexer_exp.github_issues,
    label_id        bigint                  not null
        references indexer_exp.github_labels,
    tech_created_at timestamp default now() not null,
    tech_updated_at timestamp default now() not null,
    primary key (issue_id, label_id)
);

create table indexer_exp.github_user_file_extensions
(
    user_id            bigint                                 not null,
    file_extension     text                                   not null,
    commit_count       integer                                not null,
    file_count         integer                                not null,
    modification_count integer                                not null,
    tech_created_at    timestamp with time zone default now() not null,
    tech_updated_at    timestamp with time zone default now() not null,
    primary key (user_id, file_extension)
);

create table indexer_exp.github_commits
(
    sha             text                                   not null
        primary key,
    author_id       bigint,
    tech_created_at timestamp with time zone default now() not null,
    tech_updated_at timestamp with time zone default now() not null
);

create table indexer_exp.github_pull_requests_commits
(
    commit_sha      text                                   not null
        references indexer_exp.github_commits,
    pull_request_id bigint                                 not null
        references indexer_exp.github_pull_requests,
    tech_created_at timestamp with time zone default now() not null,
    tech_updated_at timestamp with time zone default now() not null,
    primary key (commit_sha, pull_request_id)
);

create table indexer_exp.grouped_contributions
(
    contribution_uuid        uuid                            not null
        primary key,
    repo_id                  bigint                          not null
        references indexer_exp.github_repos,
    type                     indexer_exp.contribution_type   not null,
    status                   indexer_exp.contribution_status not null,
    pull_request_id          bigint
        references indexer_exp.github_pull_requests,
    issue_id                 bigint
        references indexer_exp.github_issues,
    code_review_id           text
        references indexer_exp.github_code_reviews,
    created_at               timestamp                       not null,
    updated_at               timestamp with time zone        not null,
    completed_at             timestamp,
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
    pr_review_state          indexer_exp.github_pull_request_review_state,
    main_file_extensions     text[],
    tech_created_at          timestamp default now()         not null,
    tech_updated_at          timestamp default now()         not null,
    constraint grouped_contributions_check
        check (((type = 'PULL_REQUEST'::indexer_exp.contribution_type) AND (pull_request_id IS NOT NULL) AND (issue_id IS NULL) AND (code_review_id IS NULL)) OR
               ((type = 'ISSUE'::indexer_exp.contribution_type) AND (pull_request_id IS NULL) AND (issue_id IS NOT NULL) AND (code_review_id IS NULL)) OR
               ((type = 'CODE_REVIEW'::indexer_exp.contribution_type) AND (pull_request_id IS NULL) AND (issue_id IS NULL) AND (code_review_id IS NOT NULL)))
);

create index grouped_contributions_code_review_id_as_text_index
    on indexer_exp.grouped_contributions (code_review_id);

create index grouped_contributions_completed_at_created_at_desc_idx
    on indexer_exp.grouped_contributions (COALESCE(completed_at, created_at) desc);

create index grouped_contributions_completed_at_created_at_idx
    on indexer_exp.grouped_contributions (COALESCE(completed_at, created_at));

create index grouped_contributions_completed_at_index
    on indexer_exp.grouped_contributions (completed_at);

create index grouped_contributions_created_at_index
    on indexer_exp.grouped_contributions (created_at);

create index grouped_contributions_github_author_id_index
    on indexer_exp.grouped_contributions (github_author_id);

create index grouped_contributions_pull_request_id_index
    on indexer_exp.grouped_contributions (pull_request_id);

create index grouped_contributions_repo_id_index
    on indexer_exp.grouped_contributions (repo_id);

create index grouped_contributions_status_as_text_index
    on indexer_exp.grouped_contributions (status);

create index grouped_contributions_type_as_text_index
    on indexer_exp.grouped_contributions (type);

create index grouped_contributions_coalesce_idx
    on indexer_exp.grouped_contributions (COALESCE(issue_id::text, pull_request_id::text, code_review_id));

create table indexer_exp.grouped_contribution_contributors
(
    contribution_uuid uuid                    not null
        references indexer_exp.grouped_contributions,
    contributor_id    bigint                  not null
        references indexer_exp.github_accounts,
    tech_created_at   timestamp default now() not null,
    tech_updated_at   timestamp default now() not null,
    primary key (contribution_uuid, contributor_id)
);

create unique index grouped_contribution_contributors_pk_inv
    on indexer_exp.grouped_contribution_contributors (contributor_id, contribution_uuid);

