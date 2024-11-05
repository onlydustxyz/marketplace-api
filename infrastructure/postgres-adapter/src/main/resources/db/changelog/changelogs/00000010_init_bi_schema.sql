create schema if not exists bi;

create table if not exists bi.p_reward_data
(
    reward_id            uuid                                   not null
        primary key,
    timestamp            timestamp,
    day_timestamp        timestamp,
    week_timestamp       timestamp,
    month_timestamp      timestamp,
    quarter_timestamp    timestamp,
    year_timestamp       timestamp,
    contributor_id       bigint,
    requestor_id         uuid,
    project_id           uuid,
    project_slug         text,
    usd_amount           numeric,
    amount               numeric,
    currency_id          uuid,
    ecosystem_ids        uuid[],
    program_ids          uuid[],
    language_ids         uuid[],
    project_category_ids uuid[],
    search               text,
    hash                 text,
    tech_created_at      timestamp with time zone default now() not null,
    tech_updated_at      timestamp with time zone default now() not null
);

create unique index if not exists bi_p_reward_data_reward_id_hash_idx
    on bi.p_reward_data (reward_id, hash);

create index if not exists bi_p_reward_data_project_id_timestamp_idx
    on bi.p_reward_data (project_id, timestamp, currency_id);

create index if not exists bi_p_reward_data_project_id_day_timestamp_idx
    on bi.p_reward_data (project_id, day_timestamp, currency_id);

create index if not exists bi_p_reward_data_project_id_week_timestamp_idx
    on bi.p_reward_data (project_id, week_timestamp, currency_id);

create index if not exists bi_p_reward_data_project_id_month_timestamp_idx
    on bi.p_reward_data (project_id, month_timestamp, currency_id);

create index if not exists bi_p_reward_data_project_id_quarter_timestamp_idx
    on bi.p_reward_data (project_id, quarter_timestamp, currency_id);

create index if not exists bi_p_reward_data_project_id_year_timestamp_idx
    on bi.p_reward_data (project_id, year_timestamp, currency_id);

create index if not exists bi_p_reward_data_project_id_timestamp_idx_inv
    on bi.p_reward_data (timestamp, project_id, currency_id);

create index if not exists bi_p_reward_data_project_id_day_timestamp_idx_inv
    on bi.p_reward_data (day_timestamp, project_id, currency_id);

create index if not exists bi_p_reward_data_project_id_week_timestamp_idx_inv
    on bi.p_reward_data (week_timestamp, project_id, currency_id);

create index if not exists bi_p_reward_data_project_id_month_timestamp_idx_inv
    on bi.p_reward_data (month_timestamp, project_id, currency_id);

create index if not exists bi_p_reward_data_project_id_quarter_timestamp_idx_inv
    on bi.p_reward_data (quarter_timestamp, project_id, currency_id);

create index if not exists bi_p_reward_data_project_id_year_timestamp_idx_inv
    on bi.p_reward_data (year_timestamp, project_id, currency_id);

create index if not exists bi_p_reward_data_contributor_id_timestamp_idx
    on bi.p_reward_data (contributor_id, timestamp, currency_id);

create index if not exists bi_p_reward_data_contributor_id_day_timestamp_idx
    on bi.p_reward_data (contributor_id, day_timestamp, currency_id);

create index if not exists bi_p_reward_data_contributor_id_week_timestamp_idx
    on bi.p_reward_data (contributor_id, week_timestamp, currency_id);

create index if not exists bi_p_reward_data_contributor_id_month_timestamp_idx
    on bi.p_reward_data (contributor_id, month_timestamp, currency_id);

create index if not exists bi_p_reward_data_contributor_id_quarter_timestamp_idx
    on bi.p_reward_data (contributor_id, quarter_timestamp, currency_id);

create index if not exists bi_p_reward_data_contributor_id_year_timestamp_idx
    on bi.p_reward_data (contributor_id, year_timestamp, currency_id);

create index if not exists bi_p_reward_data_contributor_id_timestamp_idx_inv
    on bi.p_reward_data (timestamp, contributor_id, currency_id);

create index if not exists bi_p_reward_data_contributor_id_day_timestamp_idx_inv
    on bi.p_reward_data (day_timestamp, contributor_id, currency_id);

create index if not exists bi_p_reward_data_contributor_id_week_timestamp_idx_inv
    on bi.p_reward_data (week_timestamp, contributor_id, currency_id);

create index if not exists bi_p_reward_data_contributor_id_month_timestamp_idx_inv
    on bi.p_reward_data (month_timestamp, contributor_id, currency_id);

create index if not exists bi_p_reward_data_contributor_id_quarter_timestamp_idx_inv
    on bi.p_reward_data (quarter_timestamp, contributor_id, currency_id);

create index if not exists bi_p_reward_data_contributor_id_year_timestamp_idx_inv
    on bi.p_reward_data (year_timestamp, contributor_id, currency_id);

create index if not exists bi_reward_data_timestamp_program_ecosystem_project_idx
    on bi.p_reward_data (timestamp, program_ids, ecosystem_ids, project_id);

create unique index if not exists p_reward_data_contributor_id_reward_id_usd_amount_uindex
    on bi.p_reward_data (contributor_id, reward_id, usd_amount);

create trigger bi_reward_data_set_tech_updated_at
    before update
    on bi.p_reward_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_project_grants_data
(
    transaction_id    uuid                                   not null
        primary key,
    project_id        uuid,
    program_id        uuid,
    currency_id       uuid,
    timestamp         timestamp with time zone,
    day_timestamp     timestamp with time zone,
    week_timestamp    timestamp with time zone,
    month_timestamp   timestamp with time zone,
    quarter_timestamp timestamp with time zone,
    year_timestamp    timestamp with time zone,
    usd_amount        numeric,
    amount            numeric,
    ecosystem_ids     uuid[],
    search            text,
    hash              text,
    tech_created_at   timestamp with time zone default now() not null,
    tech_updated_at   timestamp with time zone default now() not null
);

create unique index if not exists bi_p_project_grants_data_transaction_id_hash_idx
    on bi.p_project_grants_data (transaction_id, hash);

create unique index if not exists bi_project_grants_data_project_timestamp_program_currency_idx
    on bi.p_project_grants_data (project_id, timestamp, program_id, currency_id);

create index if not exists bi_project_grants_data_timestamp_idx
    on bi.p_project_grants_data (timestamp, project_id);

create index if not exists bi_project_grants_data_day_timestamp_idx
    on bi.p_project_grants_data (day_timestamp, project_id);

create index if not exists bi_project_grants_data_week_timestamp_idx
    on bi.p_project_grants_data (week_timestamp, project_id);

create index if not exists bi_project_grants_data_month_timestamp_idx
    on bi.p_project_grants_data (month_timestamp, project_id);

create index if not exists bi_project_grants_data_quarter_timestamp_idx
    on bi.p_project_grants_data (quarter_timestamp, project_id);

create index if not exists bi_project_grants_data_year_timestamp_idx
    on bi.p_project_grants_data (year_timestamp, project_id);

create index if not exists bi_project_grants_data_timestamp_idx_inv
    on bi.p_project_grants_data (project_id, timestamp);

create index if not exists bi_project_grants_data_day_timestamp_idx_inv
    on bi.p_project_grants_data (project_id, day_timestamp);

create index if not exists bi_project_grants_data_week_timestamp_idx_inv
    on bi.p_project_grants_data (project_id, week_timestamp);

create index if not exists bi_project_grants_data_month_timestamp_idx_inv
    on bi.p_project_grants_data (project_id, month_timestamp);

create index if not exists bi_project_grants_data_quarter_timestamp_idx_inv
    on bi.p_project_grants_data (project_id, quarter_timestamp);

create index if not exists bi_project_grants_data_year_timestamp_idx_inv
    on bi.p_project_grants_data (project_id, year_timestamp);

create trigger bi_project_grants_data_set_tech_updated_at
    before update
    on bi.p_project_grants_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_project_global_data
(
    project_id                             uuid                                   not null
        primary key,
    project_slug                           text,
    created_at                             timestamp,
    rank                                   integer,
    project                                jsonb,
    project_name                           text,
    project_visibility                     public.project_visibility,
    project_lead_ids                       uuid[],
    invited_project_lead_ids               uuid[],
    project_category_ids                   uuid[],
    project_category_slugs                 text[],
    language_ids                           uuid[],
    language_slugs                         text[],
    ecosystem_ids                          uuid[],
    ecosystem_slugs                        text[],
    program_ids                            uuid[],
    repo_ids                               bigint[],
    tags                                   public.project_tag[],
    leads                                  jsonb,
    categories                             jsonb,
    languages                              jsonb,
    ecosystems                             jsonb,
    programs                               jsonb,
    has_repos_without_github_app_installed boolean,
    search                                 text,
    hash                                   text,
    tech_created_at                        timestamp with time zone default now() not null,
    tech_updated_at                        timestamp with time zone default now() not null
);

create unique index if not exists bi_p_project_global_data_project_id_hash_idx
    on bi.p_project_global_data (project_id, hash);

create unique index if not exists p_project_global_data_project_slug_idx
    on bi.p_project_global_data (project_slug);

create trigger bi_project_global_data_set_tech_updated_at
    before update
    on bi.p_project_global_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_project_budget_data
(
    project_id               uuid                                   not null
        primary key,
    available_budget_usd     numeric,
    percent_spent_budget_usd numeric,
    budget                   jsonb,
    hash                     text,
    tech_created_at          timestamp with time zone default now() not null,
    tech_updated_at          timestamp with time zone default now() not null
);

create unique index if not exists bi_p_project_budget_data_project_id_hash_idx
    on bi.p_project_budget_data (project_id, hash);

create unique index if not exists p_project_budget_data_project_id_available_budget_usd_perce_idx
    on bi.p_project_budget_data (project_id, available_budget_usd, percent_spent_budget_usd);

create trigger bi_project_budget_data_set_tech_updated_at
    before update
    on bi.p_project_budget_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_contribution_data
(
    contribution_uuid    uuid                                   not null
        primary key,
    repo_id              bigint,
    project_id           uuid,
    project_slug         text,
    timestamp            timestamp,
    contribution_status  indexer_exp.contribution_status,
    contribution_type    indexer_exp.contribution_type,
    github_id            text,
    github_author_id     bigint,
    github_number        bigint,
    github_status        text,
    github_title         text,
    github_html_url      text,
    github_body          text,
    created_at           timestamp,
    updated_at           timestamp with time zone,
    completed_at         timestamp,
    issue_id             bigint,
    pull_request_id      bigint,
    code_review_id       text,
    is_issue             integer,
    is_pr                integer,
    is_code_review       integer,
    activity_status      public.activity_status,
    language_ids         uuid[],
    ecosystem_ids        uuid[],
    program_ids          uuid[],
    project_category_ids uuid[],
    is_good_first_issue  boolean,
    github_label_ids     bigint[],
    closing_issue_ids    bigint[],
    github_repo          jsonb,
    project              jsonb,
    github_labels        jsonb,
    languages            jsonb,
    linked_issues        jsonb,
    search               text,
    hash                 text,
    tech_created_at      timestamp with time zone default now() not null,
    tech_updated_at      timestamp with time zone default now() not null
);

create unique index if not exists bi_p_contribution_data_contribution_uuid_hash_idx
    on bi.p_contribution_data (contribution_uuid, hash);

create index if not exists p_contribution_data_created_at_idx
    on bi.p_contribution_data (created_at);

create index if not exists p_contribution_data_contribution_type_idx
    on bi.p_contribution_data (contribution_type);

create index if not exists p_contribution_data_activity_status_idx
    on bi.p_contribution_data (activity_status);

create index if not exists p_contribution_data_project_id_idx
    on bi.p_contribution_data (project_id);

create index if not exists p_contribution_data_project_slug_idx
    on bi.p_contribution_data (project_slug);

create index if not exists p_contribution_data_repo_id_idx
    on bi.p_contribution_data (repo_id);

create index if not exists p_contribution_data_project_id_timestamp_idx
    on bi.p_contribution_data (project_id, timestamp);

create index if not exists p_contribution_data_timestamp_project_id_idx
    on bi.p_contribution_data (timestamp, project_id);

create index if not exists p_contribution_data_activity_status_project_id_created_at_idx
    on bi.p_contribution_data (activity_status, project_id, created_at);

create index if not exists p_contribution_data_activity_status_project_id_contribution_idx
    on bi.p_contribution_data (activity_status, project_id, contribution_type);

create index if not exists p_contribution_data_activity_status_contribution_type_creat_idx
    on bi.p_contribution_data (activity_status, contribution_type, created_at);

create unique index if not exists p_contribution_data_project_id_contribution_type_contributi_idx
    on bi.p_contribution_data (project_id, contribution_type, contribution_uuid);

create trigger bi_contribution_data_set_tech_updated_at
    before update
    on bi.p_contribution_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_contribution_reward_data
(
    contribution_uuid         uuid                                   not null
        primary key,
    project_id                uuid,
    repo_id                   bigint,
    reward_ids                uuid[],
    total_rewarded_usd_amount numeric,
    hash                      text,
    tech_created_at           timestamp with time zone default now() not null,
    tech_updated_at           timestamp with time zone default now() not null
);

create unique index if not exists bi_p_contribution_reward_data_contribution_uuid_hash_idx
    on bi.p_contribution_reward_data (contribution_uuid, hash);

create unique index if not exists p_contribution_reward_data_contribution_uuid_total_rewarded_idx
    on bi.p_contribution_reward_data (contribution_uuid, total_rewarded_usd_amount);

create unique index if not exists p_contribution_reward_data_repo_id_contribution_uuid_idx
    on bi.p_contribution_reward_data (repo_id, contribution_uuid);

create unique index if not exists p_contribution_reward_data_project_id_contribution_uuid_idx
    on bi.p_contribution_reward_data (project_id, contribution_uuid);

create index if not exists p_contribution_reward_data_reward_ids_idx
    on bi.p_contribution_reward_data using gin (reward_ids);

create trigger bi_contribution_reward_data_set_tech_updated_at
    before update
    on bi.p_contribution_reward_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_contributor_reward_data
(
    contributor_id  bigint                                 not null
        primary key,
    currency_ids    uuid[],
    currencies      jsonb,
    search          text,
    hash            text,
    tech_created_at timestamp with time zone default now() not null,
    tech_updated_at timestamp with time zone default now() not null
);

create unique index if not exists bi_p_contributor_reward_data_contributor_id_hash_idx
    on bi.p_contributor_reward_data (contributor_id, hash);

create trigger bi_contributor_reward_data_set_tech_updated_at
    before update
    on bi.p_contributor_reward_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_contributor_application_data
(
    contributor_id           bigint                                 not null
        primary key,
    applied_on_project_ids   uuid[],
    applied_on_project_slugs text[],
    hash                     text,
    tech_created_at          timestamp with time zone default now() not null,
    tech_updated_at          timestamp with time zone default now() not null
);

create unique index if not exists bi_p_contributor_application_data_contributor_id_hash_idx
    on bi.p_contributor_application_data (contributor_id, hash);

create trigger bi_contributor_application_data_set_tech_updated_at
    before update
    on bi.p_contributor_application_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_contributor_global_data
(
    contributor_id               bigint                                 not null
        primary key,
    contributor_login            text,
    contributor_user_id          uuid,
    first_project_name           text,
    maintained_project_ids       uuid[],
    contributed_on_project_ids   uuid[],
    contributed_on_project_slugs text[],
    project_category_ids         uuid[],
    language_ids                 uuid[],
    ecosystem_ids                uuid[],
    program_ids                  uuid[],
    contributor_country          text,
    contributor                  jsonb,
    maintained_projects          jsonb,
    projects                     jsonb,
    languages                    jsonb,
    ecosystems                   jsonb,
    categories                   jsonb,
    programs                     jsonb,
    search                       text,
    hash                         text,
    tech_created_at              timestamp with time zone default now() not null,
    tech_updated_at              timestamp with time zone default now() not null
);

create unique index if not exists bi_p_contributor_global_data_contributor_id_hash_idx
    on bi.p_contributor_global_data (contributor_id, hash);

create index if not exists bi_p_contributor_global_data_contributed_on_project_ids_idx
    on bi.p_contributor_global_data using gin (contributed_on_project_ids);

create trigger bi_contributor_global_data_set_tech_updated_at
    before update
    on bi.p_contributor_global_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_contribution_contributors_data
(
    contribution_uuid uuid                                   not null
        primary key,
    repo_id           bigint,
    github_author_id  bigint,
    contributor_ids   bigint[],
    assignee_ids      bigint[],
    applicant_ids     bigint[],
    github_author     jsonb,
    contributors      jsonb,
    applicants        jsonb,
    search            text,
    hash              text,
    tech_created_at   timestamp with time zone default now() not null,
    tech_updated_at   timestamp with time zone default now() not null
);

create unique index if not exists bi_p_contribution_contributors_data_contribution_uuid_hash_idx
    on bi.p_contribution_contributors_data (contribution_uuid, hash);

create index if not exists p_contribution_contributors_data_repo_id_idx
    on bi.p_contribution_contributors_data (repo_id);

create index if not exists p_contribution_contributors_data_contributor_ids_idx
    on bi.p_contribution_contributors_data using gin (contributor_ids);

create trigger bi_contribution_contributors_data_set_tech_updated_at
    before update
    on bi.p_contribution_contributors_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_per_contributor_contribution_data
(
    technical_id                      uuid                                   not null
        primary key,
    contribution_uuid                 uuid,
    repo_id                           bigint,
    project_id                        uuid,
    project_slug                      text,
    contributor_id                    bigint,
    contributor_user_id               uuid,
    contributor_country               text,
    timestamp                         timestamp,
    contribution_status               indexer_exp.contribution_status,
    contribution_type                 indexer_exp.contribution_type,
    github_author_id                  bigint,
    github_number                     bigint,
    github_status                     text,
    github_title                      text,
    github_html_url                   text,
    github_body                       text,
    created_at                        timestamp,
    updated_at                        timestamp with time zone,
    completed_at                      timestamp,
    day_timestamp                     timestamp,
    week_timestamp                    timestamp,
    month_timestamp                   timestamp,
    quarter_timestamp                 timestamp,
    year_timestamp                    timestamp,
    is_first_contribution_on_onlydust boolean,
    is_issue                          integer,
    is_pr                             integer,
    is_code_review                    integer,
    activity_status                   public.activity_status,
    language_ids                      uuid[],
    ecosystem_ids                     uuid[],
    program_ids                       uuid[],
    project_category_ids              uuid[],
    languages                         jsonb,
    is_good_first_issue               boolean,
    assignee_ids                      bigint[],
    github_label_ids                  bigint[],
    closing_issue_ids                 bigint[],
    applicant_ids                     bigint[],
    hash                              text,
    tech_created_at                   timestamp with time zone default now() not null,
    tech_updated_at                   timestamp with time zone default now() not null
);

create unique index if not exists bi_p_per_contributor_contribution_data_technical_id_hash_idx
    on bi.p_per_contributor_contribution_data (technical_id, hash);

create index if not exists p_per_contributor_contribution_data_contribution_uuid_idx
    on bi.p_per_contributor_contribution_data (contribution_uuid);

create index if not exists p_per_contributor_contribution_data_project_id_idx
    on bi.p_per_contributor_contribution_data (project_id);

create index if not exists p_per_contributor_contribution_data_project_slug_idx
    on bi.p_per_contributor_contribution_data (project_slug);

create unique index if not exists p_per_contributor_contributio_contributor_id_contribution_u_idx
    on bi.p_per_contributor_contribution_data (contributor_id, contribution_uuid);

create unique index if not exists p_per_contributor_contributio_contributor_user_id_contribut_idx
    on bi.p_per_contributor_contribution_data (contributor_user_id, contribution_uuid);

create index if not exists bi_contribution_data_repo_id_idx
    on bi.p_per_contributor_contribution_data (repo_id);

create index if not exists bi_contribution_data_project_id_timestamp_idx
    on bi.p_per_contributor_contribution_data (project_id, timestamp);

create index if not exists bi_contribution_data_project_id_day_timestamp_idx
    on bi.p_per_contributor_contribution_data (project_id, day_timestamp);

create index if not exists bi_contribution_data_project_id_week_timestamp_idx
    on bi.p_per_contributor_contribution_data (project_id, week_timestamp);

create index if not exists bi_contribution_data_project_id_month_timestamp_idx
    on bi.p_per_contributor_contribution_data (project_id, month_timestamp);

create index if not exists bi_contribution_data_project_id_quarter_timestamp_idx
    on bi.p_per_contributor_contribution_data (project_id, quarter_timestamp);

create index if not exists bi_contribution_data_project_id_year_timestamp_idx
    on bi.p_per_contributor_contribution_data (project_id, year_timestamp);

create index if not exists bi_contribution_data_project_id_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (timestamp, project_id);

create index if not exists bi_contribution_data_project_id_day_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (day_timestamp, project_id);

create index if not exists bi_contribution_data_project_id_week_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (week_timestamp, project_id);

create index if not exists bi_contribution_data_project_id_month_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (month_timestamp, project_id);

create index if not exists bi_contribution_data_project_id_quarter_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (quarter_timestamp, project_id);

create index if not exists bi_contribution_data_project_id_year_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (year_timestamp, project_id);

create index if not exists bi_contribution_data_contributor_id_timestamp_idx
    on bi.p_per_contributor_contribution_data (contributor_id, timestamp);

create index if not exists bi_contribution_data_contributor_id_day_timestamp_idx
    on bi.p_per_contributor_contribution_data (contributor_id, day_timestamp);

create index if not exists bi_contribution_data_contributor_id_week_timestamp_idx
    on bi.p_per_contributor_contribution_data (contributor_id, week_timestamp);

create index if not exists bi_contribution_data_contributor_id_month_timestamp_idx
    on bi.p_per_contributor_contribution_data (contributor_id, month_timestamp);

create index if not exists bi_contribution_data_contributor_id_quarter_timestamp_idx
    on bi.p_per_contributor_contribution_data (contributor_id, quarter_timestamp);

create index if not exists bi_contribution_data_contributor_id_year_timestamp_idx
    on bi.p_per_contributor_contribution_data (contributor_id, year_timestamp);

create index if not exists bi_contribution_data_contributor_id_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (timestamp, contributor_id);

create index if not exists bi_contribution_data_contributor_id_day_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (day_timestamp, contributor_id);

create index if not exists bi_contribution_data_contributor_id_week_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (week_timestamp, contributor_id);

create index if not exists bi_contribution_data_contributor_id_month_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (month_timestamp, contributor_id);

create index if not exists bi_contribution_data_contributor_id_quarter_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (quarter_timestamp, contributor_id);

create index if not exists bi_contribution_data_contributor_id_year_timestamp_idx_inv
    on bi.p_per_contributor_contribution_data (year_timestamp, contributor_id);

create index if not exists bi_p_per_contributor_contribution_data_cid_pid_
    on bi.p_per_contributor_contribution_data (contributor_id asc, project_id asc, timestamp desc);

create trigger bi_per_contributor_contribution_data_set_tech_updated_at
    before update
    on bi.p_per_contributor_contribution_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_project_contributions_data
(
    project_id             uuid                                   not null
        primary key,
    repo_ids               bigint[],
    contributor_count      bigint,
    good_first_issue_count bigint,
    hash                   text,
    tech_created_at        timestamp with time zone default now() not null,
    tech_updated_at        timestamp with time zone default now() not null
);

create unique index if not exists bi_p_project_contributions_data_project_id_hash_idx
    on bi.p_project_contributions_data (project_id, hash);

create unique index if not exists p_project_contributions_data_project_id_contributor_count_g_idx
    on bi.p_project_contributions_data (project_id, contributor_count, good_first_issue_count);

create index if not exists p_project_contributions_data_repo_ids_idx
    on bi.p_project_contributions_data using gin (repo_ids);

create trigger bi_project_contributions_data_set_tech_updated_at
    before update
    on bi.p_project_contributions_data
    for each row
execute procedure public.set_tech_updated_at();

create table if not exists bi.p_application_data
(
    application_id       uuid                                   not null
        primary key,
    contribution_uuid    uuid,
    timestamp            timestamp,
    day_timestamp        timestamp,
    week_timestamp       timestamp,
    month_timestamp      timestamp,
    quarter_timestamp    timestamp,
    year_timestamp       timestamp,
    contributor_id       bigint,
    origin               application_origin,
    status               application_status,
    project_id           uuid,
    project_slug         text,
    repo_id              bigint,
    ecosystem_ids        uuid[],
    program_ids          uuid[],
    language_ids         uuid[],
    project_category_ids uuid[],
    search               text,
    hash                 text,
    tech_created_at      timestamp with time zone default now() not null,
    tech_updated_at      timestamp with time zone default now() not null
);

create unique index if not exists bi_p_application_data_application_id_hash_idx
    on bi.p_application_data (application_id, hash);

create index if not exists p_application_data_contribution_uuid_idx
    on bi.p_application_data (contribution_uuid);

create index if not exists p_application_data_repo_id_idx
    on bi.p_application_data (repo_id);

create index if not exists bi_p_application_data_project_id_timestamp_idx
    on bi.p_application_data (project_id, timestamp);

create index if not exists bi_p_application_data_project_id_day_timestamp_idx
    on bi.p_application_data (project_id, day_timestamp);

create index if not exists bi_p_application_data_project_id_week_timestamp_idx
    on bi.p_application_data (project_id, week_timestamp);

create index if not exists bi_p_application_data_project_id_month_timestamp_idx
    on bi.p_application_data (project_id, month_timestamp);

create index if not exists bi_p_application_data_project_id_quarter_timestamp_idx
    on bi.p_application_data (project_id, quarter_timestamp);

create index if not exists bi_p_application_data_project_id_year_timestamp_idx
    on bi.p_application_data (project_id, year_timestamp);

create index if not exists bi_p_application_data_project_id_timestamp_idx_inv
    on bi.p_application_data (timestamp, project_id);

create index if not exists bi_p_application_data_project_id_day_timestamp_idx_inv
    on bi.p_application_data (day_timestamp, project_id);

create index if not exists bi_p_application_data_project_id_week_timestamp_idx_inv
    on bi.p_application_data (week_timestamp, project_id);

create index if not exists bi_p_application_data_project_id_month_timestamp_idx_inv
    on bi.p_application_data (month_timestamp, project_id);

create index if not exists bi_p_application_data_project_id_quarter_timestamp_idx_inv
    on bi.p_application_data (quarter_timestamp, project_id);

create index if not exists bi_p_application_data_project_id_year_timestamp_idx_inv
    on bi.p_application_data (year_timestamp, project_id);

create index if not exists bi_p_application_data_contributor_id_timestamp_idx
    on bi.p_application_data (contributor_id, timestamp);

create index if not exists bi_p_application_data_contributor_id_day_timestamp_idx
    on bi.p_application_data (contributor_id, day_timestamp);

create index if not exists bi_p_application_data_contributor_id_week_timestamp_idx
    on bi.p_application_data (contributor_id, week_timestamp);

create index if not exists bi_p_application_data_contributor_id_month_timestamp_idx
    on bi.p_application_data (contributor_id, month_timestamp);

create index if not exists bi_p_application_data_contributor_id_quarter_timestamp_idx
    on bi.p_application_data (contributor_id, quarter_timestamp);

create index if not exists bi_p_application_data_contributor_id_year_timestamp_idx
    on bi.p_application_data (contributor_id, year_timestamp);

create index if not exists bi_p_application_data_contributor_id_timestamp_idx_inv
    on bi.p_application_data (timestamp, contributor_id);

create index if not exists bi_p_application_data_contributor_id_day_timestamp_idx_inv
    on bi.p_application_data (day_timestamp, contributor_id);

create index if not exists bi_p_application_data_contributor_id_week_timestamp_idx_inv
    on bi.p_application_data (week_timestamp, contributor_id);

create index if not exists bi_p_application_data_contributor_id_month_timestamp_idx_inv
    on bi.p_application_data (month_timestamp, contributor_id);

create index if not exists bi_p_application_data_contributor_id_quarter_timestamp_idx_inv
    on bi.p_application_data (quarter_timestamp, contributor_id);

create index if not exists bi_p_application_data_contributor_id_year_timestamp_idx_inv
    on bi.p_application_data (year_timestamp, contributor_id);

create unique index if not exists p_application_data_contributor_id_status_application_id_uindex
    on bi.p_application_data (contributor_id, status, application_id);

create trigger bi_application_data_set_tech_updated_at
    before update
    on bi.p_application_data
    for each row
execute procedure public.set_tech_updated_at();

create materialized view if not exists bi.project_contribution_stats as
WITH contributors_stats AS (SELECT contributions.contributor_id,
                                   min(contributions.created_at) AS first
                            FROM indexer_exp.contributions
                            WHERE contributions.status = 'COMPLETED'::indexer_exp.contribution_status
                            GROUP BY contributions.contributor_id)
SELECT pgr.project_id,
       count(DISTINCT c.id) FILTER (WHERE c.status = 'COMPLETED'::indexer_exp.contribution_status AND
                                          c.type = 'PULL_REQUEST'::indexer_exp.contribution_type AND c.created_at >
                                                                                                     (now() - '3 mons'::interval))   AS current_period_merged_pr_count,
       count(DISTINCT c.id) FILTER (WHERE c.status = 'COMPLETED'::indexer_exp.contribution_status AND
                                          c.type = 'PULL_REQUEST'::indexer_exp.contribution_type AND
                                          c.created_at >= (now() - '6 mons'::interval) AND c.created_at <=
                                                                                           (now() - '3 mons'::interval))             AS last_period_merged_pr_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE c.status = 'COMPLETED'::indexer_exp.contribution_status AND
                                                      c.created_at >
                                                      (now() - '3 mons'::interval))                                                  AS current_period_active_contributor_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE c.status = 'COMPLETED'::indexer_exp.contribution_status AND
                                                      c.created_at >= (now() - '6 mons'::interval) AND c.created_at <=
                                                                                                       (now() - '3 mons'::interval)) AS last_period_active_contributor_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE c.status = 'COMPLETED'::indexer_exp.contribution_status AND
                                                      cs.first > (now() - '3 mons'::interval) AND c.created_at >
                                                                                                  (now() - '3 mons'::interval))      AS current_period_new_contributor_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE c.status = 'COMPLETED'::indexer_exp.contribution_status AND
                                                      cs.first >= (now() - '6 mons'::interval) AND
                                                      cs.first <= (now() - '3 mons'::interval) AND
                                                      c.created_at >= (now() - '6 mons'::interval) AND c.created_at <=
                                                                                                       (now() - '3 mons'::interval)) AS last_period_new_contributor_count
FROM indexer_exp.contributions c
         JOIN contributors_stats cs ON cs.contributor_id = c.contributor_id
         JOIN public.project_github_repos pgr ON pgr.github_repo_id = c.repo_id
GROUP BY pgr.project_id;

create unique index if not exists project_contribution_stats_pk
    on bi.project_contribution_stats (project_id);

create materialized view if not exists bi.project_reward_stats as
SELECT r.project_id,
       avg(rsd.amount_usd_equivalent) AS average_reward_usd_amount
FROM public.rewards r
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
GROUP BY r.project_id;

create unique index if not exists project_reward_stats_pk
    on bi.project_reward_stats (project_id);

create or replace view bi.v_reward_data
            (reward_id, timestamp, day_timestamp, week_timestamp, month_timestamp, quarter_timestamp, year_timestamp,
             contributor_id, requestor_id, project_id, project_slug, usd_amount, amount, currency_id, ecosystem_ids,
             program_ids, language_ids, project_category_ids, search, hash)
as
SELECT v.reward_id,
       v."timestamp",
       v.day_timestamp,
       v.week_timestamp,
       v.month_timestamp,
       v.quarter_timestamp,
       v.year_timestamp,
       v.contributor_id,
       v.requestor_id,
       v.project_id,
       v.project_slug,
       v.usd_amount,
       v.amount,
       v.currency_id,
       v.ecosystem_ids,
       v.program_ids,
       v.language_ids,
       v.project_category_ids,
       v.search,
       md5(v.*::text) AS hash
FROM (SELECT r.id                                                                     AS reward_id,
             r.requested_at                                                           AS "timestamp",
             date_trunc('day'::text, r.requested_at)                                  AS day_timestamp,
             date_trunc('week'::text, r.requested_at)                                 AS week_timestamp,
             date_trunc('month'::text, r.requested_at)                                AS month_timestamp,
             date_trunc('quarter'::text, r.requested_at)                              AS quarter_timestamp,
             date_trunc('year'::text, r.requested_at)                                 AS year_timestamp,
             r.recipient_id                                                           AS contributor_id,
             r.requestor_id,
             r.project_id,
             p.slug                                                                   AS project_slug,
             rsd.amount_usd_equivalent                                                AS usd_amount,
             r.amount,
             r.currency_id,
             array_agg(DISTINCT pe.ecosystem_id)
             FILTER (WHERE pe.ecosystem_id IS NOT NULL)                               AS ecosystem_ids,
             array_agg(DISTINCT pp.program_id)
             FILTER (WHERE pp.program_id IS NOT NULL)                                 AS program_ids,
             array_agg(DISTINCT lfe.language_id)
             FILTER (WHERE lfe.language_id IS NOT NULL)                               AS language_ids,
             array_agg(DISTINCT ppc.project_category_id)
             FILTER (WHERE ppc.project_category_id IS NOT NULL)                       AS project_category_ids,
             string_agg((currencies.name || ' '::text) || currencies.code, ' '::text) AS search
      FROM rewards r
               JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
               JOIN projects p ON p.id = r.project_id
               JOIN currencies ON currencies.id = r.currency_id
               LEFT JOIN projects_ecosystems pe ON pe.project_id = r.project_id
               LEFT JOIN m_programs_projects pp ON pp.project_id = r.project_id
               LEFT JOIN projects_project_categories ppc ON ppc.project_id = r.project_id
               LEFT JOIN reward_items ri ON r.id = ri.reward_id
               LEFT JOIN indexer_exp.contributions c
                         ON c.contributor_id = ri.recipient_id AND c.repo_id = ri.repo_id AND
                            c.github_number = ri.number AND c.type::text::contribution_type = ri.type
               LEFT JOIN language_file_extensions lfe ON lfe.extension = ANY (c.main_file_extensions)
      GROUP BY r.id, r.requested_at, r.recipient_id, r.project_id, rsd.amount_usd_equivalent, r.amount, r.currency_id,
               p.slug) v;

create materialized view if not exists bi.m_reward_data as
SELECT v_reward_data.reward_id,
       v_reward_data."timestamp",
       v_reward_data.day_timestamp,
       v_reward_data.week_timestamp,
       v_reward_data.month_timestamp,
       v_reward_data.quarter_timestamp,
       v_reward_data.year_timestamp,
       v_reward_data.contributor_id,
       v_reward_data.requestor_id,
       v_reward_data.project_id,
       v_reward_data.project_slug,
       v_reward_data.usd_amount,
       v_reward_data.amount,
       v_reward_data.currency_id,
       v_reward_data.ecosystem_ids,
       v_reward_data.program_ids,
       v_reward_data.language_ids,
       v_reward_data.project_category_ids,
       v_reward_data.search,
       v_reward_data.hash
FROM bi.v_reward_data;

create unique index if not exists bi_m_reward_data_pk
    on bi.m_reward_data (reward_id);

create unique index if not exists bi_m_reward_data_reward_id_hash_idx
    on bi.m_reward_data (reward_id, hash);

create or replace view bi.v_project_grants_data
            (transaction_id, project_id, program_id, currency_id, timestamp, day_timestamp, week_timestamp,
             month_timestamp, quarter_timestamp, year_timestamp, usd_amount, amount, ecosystem_ids, search, hash)
as
SELECT v.transaction_id,
       v.project_id,
       v.program_id,
       v.currency_id,
       v."timestamp",
       v.day_timestamp,
       v.week_timestamp,
       v.month_timestamp,
       v.quarter_timestamp,
       v.year_timestamp,
       v.usd_amount,
       v.amount,
       v.ecosystem_ids,
       v.search,
       md5(v.*::text) AS hash
FROM (SELECT abt.id                                                                         AS transaction_id,
             abt.project_id,
             abt.program_id,
             abt.currency_id,
             abt."timestamp",
             date_trunc('day'::text, abt."timestamp")                                       AS day_timestamp,
             date_trunc('week'::text, abt."timestamp")                                      AS week_timestamp,
             date_trunc('month'::text, abt."timestamp")                                     AS month_timestamp,
             date_trunc('quarter'::text, abt."timestamp")                                   AS quarter_timestamp,
             date_trunc('year'::text, abt."timestamp")                                      AS year_timestamp,
             CASE
                 WHEN abt.type = 'TRANSFER'::accounting.transaction_type THEN abt.amount * hq.usd_conversion_rate
                 ELSE abt.amount * hq.usd_conversion_rate * '-1'::integer::numeric
                 END                                                                        AS usd_amount,
             CASE
                 WHEN abt.type = 'TRANSFER'::accounting.transaction_type THEN abt.amount
                 ELSE abt.amount * '-1'::integer::numeric
                 END                                                                        AS amount,
             array_agg(DISTINCT pe.ecosystem_id) FILTER (WHERE pe.ecosystem_id IS NOT NULL) AS ecosystem_ids,
             COALESCE(prog.name, ''::text)                                                  AS search
      FROM accounting.all_transactions abt
               JOIN LATERAL ( SELECT accounting.usd_quote_at(abt.currency_id, abt."timestamp") AS usd_conversion_rate) hq
                    ON true
               JOIN programs prog ON prog.id = abt.program_id
               LEFT JOIN projects_ecosystems pe ON pe.project_id = abt.project_id
      WHERE abt.project_id IS NOT NULL
        AND (abt.type = 'TRANSFER'::accounting.transaction_type OR abt.type = 'REFUND'::accounting.transaction_type)
        AND abt.reward_id IS NULL
        AND abt.payment_id IS NULL
      GROUP BY abt.id, abt.project_id, abt.program_id, abt.currency_id, abt."timestamp", abt.type, abt.amount,
               hq.usd_conversion_rate, prog.name) v;

create materialized view if not exists bi.m_project_grants_data as
SELECT v_project_grants_data.transaction_id,
       v_project_grants_data.project_id,
       v_project_grants_data.program_id,
       v_project_grants_data.currency_id,
       v_project_grants_data."timestamp",
       v_project_grants_data.day_timestamp,
       v_project_grants_data.week_timestamp,
       v_project_grants_data.month_timestamp,
       v_project_grants_data.quarter_timestamp,
       v_project_grants_data.year_timestamp,
       v_project_grants_data.usd_amount,
       v_project_grants_data.amount,
       v_project_grants_data.ecosystem_ids,
       v_project_grants_data.search,
       v_project_grants_data.hash
FROM bi.v_project_grants_data;

create unique index if not exists bi_m_project_grants_data_pk
    on bi.m_project_grants_data (transaction_id);

create unique index if not exists bi_m_project_grants_data_transaction_id_hash_idx
    on bi.m_project_grants_data (transaction_id, hash);

create or replace view bi.v_project_global_data
            (project_id, project_slug, created_at, rank, project, project_name, project_visibility, project_lead_ids,
             invited_project_lead_ids, project_category_ids, project_category_slugs, language_ids, language_slugs,
             ecosystem_ids, ecosystem_slugs, program_ids, repo_ids, tags, leads, categories, languages, ecosystems,
             programs, has_repos_without_github_app_installed, search, hash)
as
SELECT v.project_id,
       v.project_slug,
       v.created_at,
       v.rank,
       v.project,
       v.project_name,
       v.project_visibility,
       v.project_lead_ids,
       v.invited_project_lead_ids,
       v.project_category_ids,
       v.project_category_slugs,
       v.language_ids,
       v.language_slugs,
       v.ecosystem_ids,
       v.ecosystem_slugs,
       v.program_ids,
       v.repo_ids,
       v.tags,
       v.leads,
       v.categories,
       v.languages,
       v.ecosystems,
       v.programs,
       v.has_repos_without_github_app_installed,
       v.search,
       md5(v.*::text) AS hash
FROM (SELECT p.id                                                                                    AS project_id,
             p.slug                                                                                  AS project_slug,
             p.created_at,
             p.rank,
             jsonb_build_object('id', p.id, 'slug', p.slug, 'name', p.name, 'logoUrl', p.logo_url, 'shortDescription',
                                p.short_description, 'hiring', p.hiring, 'visibility', p.visibility) AS project,
             p.name                                                                                  AS project_name,
             p.visibility                                                                            AS project_visibility,
             array_agg(DISTINCT uleads.id) FILTER (WHERE uleads.id IS NOT NULL)                      AS project_lead_ids,
             array_agg(DISTINCT uinvleads.id)
             FILTER (WHERE uinvleads.id IS NOT NULL)                                                 AS invited_project_lead_ids,
             array_agg(DISTINCT pc.id) FILTER (WHERE pc.id IS NOT NULL)                              AS project_category_ids,
             array_agg(DISTINCT pc.slug) FILTER (WHERE pc.slug IS NOT NULL)                          AS project_category_slugs,
             array_agg(DISTINCT l.id) FILTER (WHERE l.id IS NOT NULL)                                AS language_ids,
             array_agg(DISTINCT l.slug) FILTER (WHERE l.slug IS NOT NULL)                            AS language_slugs,
             array_agg(DISTINCT e.id) FILTER (WHERE e.id IS NOT NULL)                                AS ecosystem_ids,
             array_agg(DISTINCT e.slug) FILTER (WHERE e.slug IS NOT NULL)                            AS ecosystem_slugs,
             array_agg(DISTINCT prog.id) FILTER (WHERE prog.id IS NOT NULL)                          AS program_ids,
             array_agg(DISTINCT pgr.github_repo_id) FILTER (WHERE pgr.github_repo_id IS NOT NULL)    AS repo_ids,
             array_agg(DISTINCT pt.tag) FILTER (WHERE pt.tag IS NOT NULL)                            AS tags,
             jsonb_agg(DISTINCT
             jsonb_build_object('id', uleads.id, 'login', uleads.github_login, 'githubUserId', uleads.github_user_id,
                                'avatarUrl', user_avatar_url(uleads.github_user_id, uleads.github_avatar_url)))
             FILTER (WHERE uleads.id IS NOT NULL)                                                    AS leads,
             jsonb_agg(DISTINCT
             jsonb_build_object('id', pc.id, 'slug', pc.slug, 'name', pc.name, 'description', pc.description,
                                'iconSlug', pc.icon_slug)) FILTER (WHERE pc.id IS NOT NULL)          AS categories,
             jsonb_agg(DISTINCT
             jsonb_build_object('id', l.id, 'slug', l.slug, 'name', l.name, 'logoUrl', l.logo_url, 'bannerUrl',
                                l.banner_url)) FILTER (WHERE l.id IS NOT NULL)                       AS languages,
             jsonb_agg(DISTINCT
             jsonb_build_object('id', e.id, 'slug', e.slug, 'name', e.name, 'logoUrl', e.logo_url, 'bannerUrl',
                                e.banner_url, 'url', e.url)) FILTER (WHERE e.id IS NOT NULL)         AS ecosystems,
             jsonb_agg(DISTINCT jsonb_build_object('id', prog.id, 'name', prog.name, 'logoUrl', prog.logo_url))
             FILTER (WHERE prog.id IS NOT NULL)                                                      AS programs,
             count(DISTINCT pgr.github_repo_id) > count(DISTINCT agr.repo_id)                        AS has_repos_without_github_app_installed,
             concat(COALESCE(string_agg(DISTINCT uleads.github_login, ' '::text), ''::text), ' ',
                    COALESCE(string_agg(DISTINCT p.name, ' '::text), ''::text), ' ',
                    COALESCE(string_agg(DISTINCT p.slug, ' '::text), ''::text), ' ',
                    COALESCE(string_agg(DISTINCT pc.name, ' '::text), ''::text), ' ',
                    COALESCE(string_agg(DISTINCT l.name, ' '::text), ''::text), ' ',
                    COALESCE(string_agg(DISTINCT e.name, ' '::text), ''::text), ' ',
                    COALESCE(string_agg(DISTINCT currencies.name, ' '::text), ''::text), ' ',
                    COALESCE(string_agg(DISTINCT currencies.code, ' '::text), ''::text), ' ',
                    COALESCE(string_agg(DISTINCT prog.name, ' '::text), ''::text))                   AS search
      FROM projects p
               LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
               LEFT JOIN ecosystems e ON e.id = pe.ecosystem_id
               LEFT JOIN project_languages pl ON pl.project_id = p.id
               LEFT JOIN languages l ON l.id = pl.language_id
               LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
               LEFT JOIN project_categories pc ON pc.id = ppc.project_category_id
               LEFT JOIN v_programs_projects pp ON pp.project_id = p.id
               LEFT JOIN programs prog ON prog.id = pp.program_id
               LEFT JOIN project_leads pleads ON pleads.project_id = p.id
               LEFT JOIN iam.users uleads ON uleads.id = pleads.user_id
               LEFT JOIN pending_project_leader_invitations ppli ON ppli.project_id = p.id
               LEFT JOIN iam.users uinvleads ON uinvleads.github_user_id = ppli.github_user_id
               LEFT JOIN projects_tags pt ON pt.project_id = p.id
               LEFT JOIN project_github_repos pgr ON pgr.project_id = p.id
               LEFT JOIN indexer_exp.authorized_github_repos agr ON agr.repo_id = pgr.github_repo_id
               LEFT JOIN LATERAL ( SELECT DISTINCT c.name,
                                                   c.code
                                   FROM bi.p_reward_data rd
                                            FULL JOIN bi.p_project_grants_data gd ON gd.project_id = rd.project_id
                                            JOIN currencies c ON c.id = COALESCE(rd.currency_id, gd.currency_id)
                                   WHERE rd.project_id = p.id
                                      OR gd.project_id = p.id) currencies ON true
      GROUP BY p.id) v;

create materialized view if not exists bi.m_project_global_data as
SELECT v_project_global_data.project_id,
       v_project_global_data.project_slug,
       v_project_global_data.created_at,
       v_project_global_data.rank,
       v_project_global_data.project,
       v_project_global_data.project_name,
       v_project_global_data.project_visibility,
       v_project_global_data.project_lead_ids,
       v_project_global_data.invited_project_lead_ids,
       v_project_global_data.project_category_ids,
       v_project_global_data.project_category_slugs,
       v_project_global_data.language_ids,
       v_project_global_data.language_slugs,
       v_project_global_data.ecosystem_ids,
       v_project_global_data.ecosystem_slugs,
       v_project_global_data.program_ids,
       v_project_global_data.repo_ids,
       v_project_global_data.tags,
       v_project_global_data.leads,
       v_project_global_data.categories,
       v_project_global_data.languages,
       v_project_global_data.ecosystems,
       v_project_global_data.programs,
       v_project_global_data.has_repos_without_github_app_installed,
       v_project_global_data.search,
       v_project_global_data.hash
FROM bi.v_project_global_data;

create unique index if not exists bi_m_project_global_data_pk
    on bi.m_project_global_data (project_id);

create unique index if not exists bi_m_project_global_data_project_id_hash_idx
    on bi.m_project_global_data (project_id, hash);

create or replace view bi.v_project_budget_data
    (project_id, available_budget_usd, percent_spent_budget_usd, budget, hash) as
SELECT v.project_id,
       v.available_budget_usd,
       v.percent_spent_budget_usd,
       v.budget,
       md5(v.*::text) AS hash
FROM (SELECT p.id                                                                      AS project_id,
             max(budgets.available_budget_usd)                                         AS available_budget_usd,
             max(budgets.percent_spent_budget_usd)                                     AS percent_spent_budget_usd,
             COALESCE(jsonb_agg(DISTINCT
                      jsonb_build_object('availableBudgetUsd', budgets.available_budget_usd, 'percentSpentBudgetUsd',
                                         budgets.percent_spent_budget_usd, 'availableBudgetPerCurrency',
                                         budgets.available_budget_per_currency, 'percentSpentBudgetPerCurrency',
                                         budgets.percent_spent_budget_per_currency, 'grantedAmountUsd',
                                         budgets.granted_amount_usd, 'grantedAmountPerCurrency',
                                         budgets.granted_amount_per_currency, 'rewardedAmountUsd',
                                         budgets.rewarded_amount_usd, 'rewardedAmountPerCurrency',
                                         budgets.rewarded_amount_per_currency))
                      FILTER (WHERE budgets.project_id IS NOT NULL), '[]'::jsonb) -> 0 AS budget
      FROM projects p
               LEFT JOIN LATERAL ( SELECT gd.project_id,
                                          COALESCE(sum(gd.current_usd_amount), 0::numeric)                           AS granted_amount_usd,
                                          COALESCE(sum(rd.usd_amount), 0::numeric)                                   AS rewarded_amount_usd,
                                          sum(COALESCE(gd.current_usd_amount, 0::numeric) -
                                              COALESCE(rd.current_usd_amount, 0::numeric))                           AS available_budget_usd,
                                          sum(COALESCE(rd.current_usd_amount, 0::numeric)) /
                                          GREATEST(sum(COALESCE(gd.current_usd_amount, 0::numeric)),
                                                   1::numeric)                                                       AS percent_spent_budget_usd,
                                          jsonb_agg(jsonb_build_object('currency', gd.currency, 'amount', gd.amount,
                                                                       'usdAmount',
                                                                       gd.current_usd_amount))                       AS granted_amount_per_currency,
                                          jsonb_agg(
                                          jsonb_build_object('currency', rd.currency, 'amount', rd.amount, 'usdAmount',
                                                             rd.usd_amount))
                                          FILTER (WHERE rd.currency_id IS NOT NULL)                                  AS rewarded_amount_per_currency,
                                          jsonb_agg(jsonb_build_object('currency', gd.currency, 'amount',
                                                                       COALESCE(gd.amount, 0::numeric) -
                                                                       COALESCE(rd.amount, 0::numeric), 'usdAmount',
                                                                       COALESCE(gd.current_usd_amount, 0::numeric) -
                                                                       COALESCE(rd.current_usd_amount, 0::numeric))) AS available_budget_per_currency,
                                          jsonb_agg(jsonb_build_object('currency', gd.currency, 'amount',
                                                                       COALESCE(gd.amount, 0::numeric) /
                                                                       COALESCE(rd.amount, 1::numeric)))             AS percent_spent_budget_per_currency
                                   FROM (SELECT gd_1.project_id,
                                                c.id                                                        AS currency_id,
                                                jsonb_build_object('id', c.id, 'code', c.code, 'name', c.name,
                                                                   'decimals', c.decimals, 'logoUrl',
                                                                   c.logo_url)                              AS currency,
                                                sum(gd_1.amount)                                            AS amount,
                                                accounting.usd_equivalent_at(sum(gd_1.amount), c.id, now()) AS current_usd_amount
                                         FROM bi.p_project_grants_data gd_1
                                                  JOIN currencies c ON c.id = gd_1.currency_id
                                         GROUP BY gd_1.project_id, c.id) gd
                                            LEFT JOIN (SELECT rd_1.project_id,
                                                              c.id                                                        AS currency_id,
                                                              jsonb_build_object('id', c.id, 'code', c.code, 'name',
                                                                                 c.name, 'decimals', c.decimals,
                                                                                 'logoUrl',
                                                                                 c.logo_url)                              AS currency,
                                                              sum(rd_1.usd_amount)                                        AS usd_amount,
                                                              sum(rd_1.amount)                                            AS amount,
                                                              accounting.usd_equivalent_at(sum(rd_1.amount), c.id, now()) AS current_usd_amount
                                                       FROM bi.p_reward_data rd_1
                                                                JOIN currencies c ON c.id = rd_1.currency_id
                                                       GROUP BY rd_1.project_id, c.id) rd
                                                      ON gd.project_id = rd.project_id AND gd.currency_id = rd.currency_id
                                   GROUP BY gd.project_id) budgets ON budgets.project_id = p.id
      GROUP BY p.id) v;

create materialized view if not exists bi.m_project_budget_data as
SELECT v_project_budget_data.project_id,
       v_project_budget_data.available_budget_usd,
       v_project_budget_data.percent_spent_budget_usd,
       v_project_budget_data.budget,
       v_project_budget_data.hash
FROM bi.v_project_budget_data;

create unique index if not exists bi_m_project_budget_data_pk
    on bi.m_project_budget_data (project_id);

create unique index if not exists bi_m_project_budget_data_project_id_hash_idx
    on bi.m_project_budget_data (project_id, hash);

create or replace view bi.v_contribution_data
            (contribution_uuid, repo_id, project_id, project_slug, timestamp, contribution_status, contribution_type,
             github_id, github_author_id, github_number, github_status, github_title, github_html_url, github_body,
             created_at, updated_at, completed_at, issue_id, pull_request_id, code_review_id, is_issue, is_pr,
             is_code_review, activity_status, language_ids, ecosystem_ids, program_ids, project_category_ids,
             is_good_first_issue, github_label_ids, closing_issue_ids, github_repo, project, github_labels, languages,
             linked_issues, search, hash)
as
SELECT v.contribution_uuid,
       v.repo_id,
       v.project_id,
       v.project_slug,
       v."timestamp",
       v.contribution_status,
       v.contribution_type,
       v.github_id,
       v.github_author_id,
       v.github_number,
       v.github_status,
       v.github_title,
       v.github_html_url,
       v.github_body,
       v.created_at,
       v.updated_at,
       v.completed_at,
       v.issue_id,
       v.pull_request_id,
       v.code_review_id,
       v.is_issue,
       v.is_pr,
       v.is_code_review,
       v.activity_status,
       v.language_ids,
       v.ecosystem_ids,
       v.program_ids,
       v.project_category_ids,
       v.is_good_first_issue,
       v.github_label_ids,
       v.closing_issue_ids,
       v.github_repo,
       v.project,
       v.github_labels,
       v.languages,
       v.linked_issues,
       v.search,
       md5(v.*::text) AS hash
FROM (WITH ranked_project_github_repos_relationship AS (SELECT project_github_repos.project_id,
                                                               project_github_repos.github_repo_id,
                                                               row_number()
                                                               OVER (PARTITION BY project_github_repos.github_repo_id ORDER BY project_github_repos.project_id) AS row_number
                                                        FROM project_github_repos)
      SELECT c.contribution_uuid,
             c.repo_id,
             p.id                                                                   AS project_id,
             p.slug                                                                 AS project_slug,
             c.created_at                                                           AS "timestamp",
             c.status                                                               AS contribution_status,
             c.type                                                                 AS contribution_type,
             COALESCE(c.pull_request_id::text, c.issue_id::text,
                      c.code_review_id)                                             AS github_id,
             c.github_author_id,
             c.github_number,
             c.github_status,
             c.github_title,
             c.github_html_url,
             c.github_body,
             c.created_at,
             c.updated_at,
             c.completed_at,
             c.issue_id,
             c.pull_request_id,
             c.code_review_id,
             (c.type = 'ISSUE'::indexer_exp.contribution_type)::integer             AS is_issue,
             (c.type = 'PULL_REQUEST'::indexer_exp.contribution_type)::integer      AS is_pr,
             (c.type = 'CODE_REVIEW'::indexer_exp.contribution_type)::integer       AS is_code_review,
             CASE
                 WHEN agc.contribution_uuid IS NOT NULL THEN 'ARCHIVED'::activity_status
                 WHEN c.type = 'ISSUE'::indexer_exp.contribution_type THEN
                     CASE
                         WHEN c.github_status = 'OPEN'::text AND bool_and(gia.user_id IS NULL) THEN 'NOT_ASSIGNED'::activity_status
                         WHEN c.github_status = 'OPEN'::text AND bool_or(gia.user_id IS NOT NULL) THEN 'IN_PROGRESS'::activity_status
                         ELSE 'DONE'::activity_status
                         END
                 WHEN c.type = 'PULL_REQUEST'::indexer_exp.contribution_type THEN
                     CASE
                         WHEN c.github_status = 'DRAFT'::text THEN 'IN_PROGRESS'::activity_status
                         WHEN c.github_status = 'OPEN'::text THEN 'TO_REVIEW'::activity_status
                         ELSE 'DONE'::activity_status
                         END
                 WHEN c.type = 'CODE_REVIEW'::indexer_exp.contribution_type THEN
                     CASE
                         WHEN c.pr_review_state = ANY
                              (ARRAY ['PENDING_REVIEWER'::indexer_exp.github_pull_request_review_state, 'UNDER_REVIEW'::indexer_exp.github_pull_request_review_state])
                             THEN 'IN_PROGRESS'::activity_status
                         ELSE 'DONE'::activity_status
                         END
                 ELSE NULL::activity_status
                 END                                                                AS activity_status,
             array_agg(DISTINCT l.id) FILTER (WHERE l.id IS NOT NULL)               AS language_ids,
             array_agg(DISTINCT pe.ecosystem_id)
             FILTER (WHERE pe.ecosystem_id IS NOT NULL)                             AS ecosystem_ids,
             array_agg(DISTINCT pp.program_id)
             FILTER (WHERE pp.program_id IS NOT NULL)                               AS program_ids,
             array_agg(DISTINCT ppc.project_category_id)
             FILTER (WHERE ppc.project_category_id IS NOT NULL)                     AS project_category_ids,
             bool_or(gl.name ~~* '%good%first%issue%'::text)                        AS is_good_first_issue,
             array_agg(DISTINCT gil.label_id)
             FILTER (WHERE gil.label_id IS NOT NULL)                                AS github_label_ids,
             array_agg(DISTINCT ci.issue_id) FILTER (WHERE ci.issue_id IS NOT NULL) AS closing_issue_ids,
             jsonb_build_object('id', gr.id, 'owner', gr.owner_login, 'name', gr.name, 'description', gr.description,
                                'htmlUrl',
                                gr.html_url)                                        AS github_repo,
             CASE
                 WHEN p.id IS NOT NULL THEN jsonb_build_object('id', p.id, 'slug', p.slug, 'name', p.name, 'logoUrl',
                                                               p.logo_url)
                 ELSE NULL::jsonb
                 END                                                                AS project,
             jsonb_agg(DISTINCT jsonb_build_object('name', gl.name, 'description', gl.description))
             FILTER (WHERE gl.id IS NOT NULL)                                       AS github_labels,
             jsonb_agg(DISTINCT
             jsonb_build_object('id', l.id, 'slug', l.slug, 'name', l.name, 'logoUrl', l.logo_url, 'bannerUrl',
                                l.banner_url))
             FILTER (WHERE l.id IS NOT NULL)                                        AS languages,
             jsonb_agg(DISTINCT
             jsonb_build_object('type', 'ISSUE', 'githubId', i.id, 'githubNumber', i.number, 'githubStatus', i.status,
                                'githubTitle', i.title, 'githubHtmlUrl', i.html_url))
             FILTER (WHERE i.id IS NOT NULL)                                        AS linked_issues,
             concat(c.github_number, ' ', c.github_title, ' ', string_agg(gl.name, ' '::text), ' ',
                    string_agg(l.name, ' '::text), ' ',
                    string_agg((i.number || ' '::text) || i.title, ' '::text))      AS search
      FROM indexer_exp.grouped_contributions c
               LEFT JOIN indexer_exp.github_repos gr ON gr.id = c.repo_id
               LEFT JOIN ranked_project_github_repos_relationship pgr
                         ON pgr.github_repo_id = c.repo_id AND pgr.row_number = 1
               LEFT JOIN projects p ON p.id = pgr.project_id
               LEFT JOIN language_file_extensions lfe ON lfe.extension = ANY (c.main_file_extensions)
               LEFT JOIN languages l ON l.id = lfe.language_id
               LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
               LEFT JOIN m_programs_projects pp ON pp.project_id = p.id
               LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
               LEFT JOIN indexer_exp.github_issues_labels gil ON gil.issue_id = c.issue_id
               LEFT JOIN indexer_exp.github_labels gl ON gil.label_id = gl.id
               LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = c.issue_id
               LEFT JOIN indexer_exp.github_code_reviews cr ON cr.id = c.code_review_id
               LEFT JOIN indexer_exp.github_pull_requests_closing_issues ci ON ci.pull_request_id = c.pull_request_id
               LEFT JOIN indexer_exp.github_issues i ON i.id = ci.issue_id
               LEFT JOIN archived_github_contributions agc ON agc.contribution_uuid = c.contribution_uuid
      GROUP BY c.contribution_uuid, c.repo_id, p.id, p.slug, c.created_at, c.type, c.status, c.pull_request_id,
               c.issue_id, c.github_number, c.github_status, c.github_title, c.github_html_url, c.github_body,
               c.pr_review_state, c.updated_at, c.completed_at, cr.pull_request_id, gr.id, agc.contribution_uuid) v;

create materialized view if not exists bi.m_contribution_data as
SELECT v_contribution_data.contribution_uuid,
       v_contribution_data.repo_id,
       v_contribution_data.project_id,
       v_contribution_data.project_slug,
       v_contribution_data."timestamp",
       v_contribution_data.contribution_status,
       v_contribution_data.contribution_type,
       v_contribution_data.github_id,
       v_contribution_data.github_author_id,
       v_contribution_data.github_number,
       v_contribution_data.github_status,
       v_contribution_data.github_title,
       v_contribution_data.github_html_url,
       v_contribution_data.github_body,
       v_contribution_data.created_at,
       v_contribution_data.updated_at,
       v_contribution_data.completed_at,
       v_contribution_data.issue_id,
       v_contribution_data.pull_request_id,
       v_contribution_data.code_review_id,
       v_contribution_data.is_issue,
       v_contribution_data.is_pr,
       v_contribution_data.is_code_review,
       v_contribution_data.activity_status,
       v_contribution_data.language_ids,
       v_contribution_data.ecosystem_ids,
       v_contribution_data.program_ids,
       v_contribution_data.project_category_ids,
       v_contribution_data.is_good_first_issue,
       v_contribution_data.github_label_ids,
       v_contribution_data.closing_issue_ids,
       v_contribution_data.github_repo,
       v_contribution_data.project,
       v_contribution_data.github_labels,
       v_contribution_data.languages,
       v_contribution_data.linked_issues,
       v_contribution_data.search,
       v_contribution_data.hash
FROM bi.v_contribution_data;

create unique index if not exists bi_m_contribution_data_pk
    on bi.m_contribution_data (contribution_uuid);

create unique index if not exists bi_m_contribution_data_contribution_uuid_hash_idx
    on bi.m_contribution_data (contribution_uuid, hash);


create or replace view bi.v_contribution_reward_data
    (contribution_uuid, project_id, repo_id, reward_ids, total_rewarded_usd_amount, hash) as
SELECT v.contribution_uuid,
       v.project_id,
       v.repo_id,
       v.reward_ids,
       v.total_rewarded_usd_amount,
       md5(v.*::text) AS hash
FROM (SELECT cd.contribution_uuid,
             cd.project_id,
             cd.repo_id,
             array_agg(ri.reward_id)                 AS reward_ids,
             sum(round(rd.amount_usd_equivalent, 2)) AS total_rewarded_usd_amount
      FROM bi.p_contribution_data cd
               JOIN reward_items ri ON ri.contribution_uuid = cd.contribution_uuid
               JOIN accounting.reward_status_data rd ON rd.reward_id = ri.reward_id
      GROUP BY cd.contribution_uuid, cd.project_id, cd.repo_id) v;

create materialized view if not exists bi.m_contribution_reward_data as
SELECT v_contribution_reward_data.contribution_uuid,
       v_contribution_reward_data.project_id,
       v_contribution_reward_data.repo_id,
       v_contribution_reward_data.reward_ids,
       v_contribution_reward_data.total_rewarded_usd_amount,
       v_contribution_reward_data.hash
FROM bi.v_contribution_reward_data;

create unique index if not exists bi_m_contribution_reward_data_pk
    on bi.m_contribution_reward_data (contribution_uuid);

create unique index if not exists bi_m_contribution_reward_data_contribution_uuid_hash_idx
    on bi.m_contribution_reward_data (contribution_uuid, hash);

create or replace view bi.v_contributor_reward_data(contributor_id, currency_ids, currencies, search, hash) as
SELECT v.contributor_id,
       v.currency_ids,
       v.currencies,
       v.search,
       md5(v.*::text) AS hash
FROM (SELECT c.contributor_id,
             c.currency_ids,
             c.currencies,
             COALESCE((SELECT concat(string_agg(cu.value ->> 'name'::text, ' '::text), ' ',
                                     string_agg(cu.value ->> 'code'::text, ' '::text)) AS concat
                       FROM jsonb_array_elements(c.currencies) cu(value)), ''::text) AS search
      FROM (SELECT c_1.contributor_id,
                   c_1.currency_ids,
                   (SELECT jsonb_agg(jsonb_build_object('id', cu.id, 'code', cu.code, 'name', cu.name)) AS json
                    FROM currencies cu
                    WHERE cu.id = ANY (c_1.currency_ids)) AS currencies
            FROM (SELECT ga.id                                                                      AS contributor_id,
                         array_agg(DISTINCT currencies.id) FILTER (WHERE currencies.id IS NOT NULL) AS currency_ids
                  FROM indexer_exp.github_accounts ga
                           LEFT JOIN rewards r ON r.recipient_id = ga.id
                           LEFT JOIN currencies ON r.currency_id = currencies.id
                  GROUP BY ga.id) c_1) c) v;

create or replace view bi.v_contributor_application_data
    (contributor_id, applied_on_project_ids, applied_on_project_slugs, hash) as
SELECT v.contributor_id,
       v.applied_on_project_ids,
       v.applied_on_project_slugs,
       md5(v.*::text) AS hash
FROM (SELECT ga.id                                                          AS contributor_id,
             array_agg(DISTINCT ap.id) FILTER (WHERE a.id IS NOT NULL)      AS applied_on_project_ids,
             array_agg(DISTINCT ap.slug) FILTER (WHERE ap.slug IS NOT NULL) AS applied_on_project_slugs
      FROM indexer_exp.github_accounts ga
               LEFT JOIN applications a ON a.applicant_id = ga.id
               LEFT JOIN projects ap ON ap.id = a.project_id
      GROUP BY ga.id) v;

create materialized view if not exists bi.m_contributor_reward_data as
SELECT v_contributor_reward_data.contributor_id,
       v_contributor_reward_data.currency_ids,
       v_contributor_reward_data.currencies,
       v_contributor_reward_data.search,
       v_contributor_reward_data.hash
FROM bi.v_contributor_reward_data;

create unique index if not exists bi_m_contributor_reward_data_pk
    on bi.m_contributor_reward_data (contributor_id);

create unique index if not exists bi_m_contributor_reward_data_contributor_id_hash_idx
    on bi.m_contributor_reward_data (contributor_id, hash);

create materialized view if not exists bi.m_contributor_application_data as
SELECT v_contributor_application_data.contributor_id,
       v_contributor_application_data.applied_on_project_ids,
       v_contributor_application_data.applied_on_project_slugs,
       v_contributor_application_data.hash
FROM bi.v_contributor_application_data;

create unique index if not exists bi_m_contributor_application_data_pk
    on bi.m_contributor_application_data (contributor_id);

create unique index if not exists bi_m_contributor_application_data_contributor_id_hash_idx
    on bi.m_contributor_application_data (contributor_id, hash);

create or replace function bi.search_of(contributor_login text, projects jsonb, categories jsonb, languages jsonb, ecosystems jsonb,
                                        programs jsonb) returns text
    stable
    language sql
as
$$
select concat(coalesce(string_agg(contributor_login, ' '), ''), ' ',
              coalesce((select concat(string_agg(p ->> 'name', ' '), ' ', string_agg(p ->> 'slug', ' ')) from jsonb_array_elements(projects) as p), ''), ' ',
              coalesce((select string_agg(pc ->> 'name', ' ') from jsonb_array_elements(categories) as pc), ''), ' ',
              coalesce((select string_agg(l ->> 'name', ' ') from jsonb_array_elements(languages) as l), ''), ' ',
              coalesce((select string_agg(e ->> 'name', ' ') from jsonb_array_elements(ecosystems) as e), ''), ' ',
              coalesce((select string_agg(prog ->> 'name', ' ') from jsonb_array_elements(programs) as prog), '')) as search

$$;

create or replace view bi.v_contributor_global_data
            (contributor_id, contributor_login, contributor_user_id, first_project_name, maintained_project_ids,
             contributed_on_project_ids, contributed_on_project_slugs, project_category_ids, language_ids,
             ecosystem_ids, program_ids, contributor_country, contributor, maintained_projects, projects, languages,
             ecosystems, categories, programs, search, hash)
as
SELECT v.contributor_id,
       v.contributor_login,
       v.contributor_user_id,
       v.first_project_name,
       v.maintained_project_ids,
       v.contributed_on_project_ids,
       v.contributed_on_project_slugs,
       v.project_category_ids,
       v.language_ids,
       v.ecosystem_ids,
       v.program_ids,
       v.contributor_country,
       v.contributor,
       v.maintained_projects,
       v.projects,
       v.languages,
       v.ecosystems,
       v.categories,
       v.programs,
       v.search,
       md5(v.*::text) AS hash
FROM (SELECT c.contributor_id,
             c.contributor_login,
             c.contributor_user_id,
             c.first_project_name,
             c.maintained_project_ids,
             c.contributed_on_project_ids,
             c.contributed_on_project_slugs,
             c.project_category_ids,
             c.language_ids,
             c.ecosystem_ids,
             c.program_ids,
             c.contributor_country,
             c.contributor,
             c.maintained_projects,
             c.projects,
             c.languages,
             c.ecosystems,
             c.categories,
             c.programs,
             bi.search_of(c.contributor_login, c.projects, c.categories, c.languages, c.ecosystems,
                          c.programs) AS search
      FROM (SELECT c_1.contributor_id,
                   c_1.contributor_login,
                   c_1.contributor_user_id,
                   c_1.first_project_name,
                   c_1.maintained_project_ids,
                   c_1.contributed_on_project_ids,
                   c_1.contributed_on_project_slugs,
                   c_1.project_category_ids,
                   c_1.language_ids,
                   c_1.ecosystem_ids,
                   c_1.program_ids,
                   (SELECT kyc.country
                    FROM iam.users u
                             JOIN accounting.billing_profiles_users bpu ON bpu.user_id = u.id
                             JOIN accounting.kyc
                                  ON kyc.billing_profile_id = bpu.billing_profile_id AND kyc.country IS NOT NULL
                    WHERE u.github_user_id = c_1.contributor_id
                    LIMIT 1)                                           AS contributor_country,
                   (SELECT jsonb_build_object('githubUserId', u.github_user_id, 'login', u.login, 'avatarUrl',
                                              u.avatar_url, 'isRegistered', u.user_id IS NOT NULL, 'id', u.user_id,
                                              'bio', u.bio, 'signedUpAt', u.signed_up_at, 'signedUpOnGithubAt',
                                              u.signed_up_on_github_at, 'globalRank', gur.rank, 'globalRankPercentile',
                                              gur.rank_percentile, 'globalRankCategory',
                                              CASE
                                                  WHEN gur.rank_percentile <= 0.02::double precision THEN 'A'::text
                                                  WHEN gur.rank_percentile <= 0.04::double precision THEN 'B'::text
                                                  WHEN gur.rank_percentile <= 0.06::double precision THEN 'C'::text
                                                  WHEN gur.rank_percentile <= 0.08::double precision THEN 'D'::text
                                                  WHEN gur.rank_percentile <= 0.10::double precision THEN 'E'::text
                                                  ELSE 'F'::text
                                                  END, 'contacts',
                                              (SELECT jsonb_agg(jsonb_build_object('channel', ci.channel, 'contact', ci.contact)) AS jsonb_agg
                                               FROM contact_informations ci
                                               WHERE ci.user_id = u.user_id)) AS json
                    FROM iam.all_users u
                             LEFT JOIN global_users_ranks gur ON gur.github_user_id = u.github_user_id
                    WHERE u.github_user_id = c_1.contributor_id)       AS contributor,
                   (SELECT jsonb_agg(jsonb_build_object('id', p.id, 'slug', p.slug, 'name', p.name, 'logoUrl',
                                                        p.logo_url)) AS jsonb_agg
                    FROM projects p
                    WHERE p.id = ANY (c_1.maintained_project_ids))     AS maintained_projects,
                   (SELECT jsonb_agg(jsonb_build_object('id', p.id, 'slug', p.slug, 'name', p.name, 'logoUrl',
                                                        p.logo_url)) AS jsonb_agg
                    FROM projects p
                    WHERE p.id = ANY (c_1.contributed_on_project_ids)) AS projects,
                   (SELECT jsonb_agg(jsonb_build_object('id', l.id, 'slug', l.slug, 'name', l.name, 'logoUrl',
                                                        l.logo_url, 'bannerUrl', l.banner_url)) AS jsonb_agg
                    FROM languages l
                    WHERE l.id = ANY (c_1.language_ids))               AS languages,
                   (SELECT jsonb_agg(jsonb_build_object('id', e.id, 'slug', e.slug, 'name', e.name, 'logoUrl',
                                                        e.logo_url, 'bannerUrl', e.banner_url, 'url',
                                                        e.url)) AS jsonb_agg
                    FROM ecosystems e
                    WHERE e.id = ANY (c_1.ecosystem_ids))              AS ecosystems,
                   (SELECT jsonb_agg(jsonb_build_object('id', pc.id, 'slug', pc.slug, 'name', pc.name, 'description',
                                                        pc.description, 'iconSlug', pc.icon_slug)) AS jsonb_agg
                    FROM project_categories pc
                    WHERE pc.id = ANY (c_1.project_category_ids))      AS categories,
                   (SELECT jsonb_agg(jsonb_build_object('id', prog.id, 'name', prog.name)) AS json
                    FROM programs prog
                    WHERE prog.id = ANY (c_1.program_ids))             AS programs
            FROM (SELECT ga.id                                                                          AS contributor_id,
                         ga.login                                                                       AS contributor_login,
                         u.id                                                                           AS contributor_user_id,
                         min(p.name)                                                                    AS first_project_name,
                         array_agg(DISTINCT pl.project_id)
                         FILTER (WHERE pl.project_id IS NOT NULL)                                       AS maintained_project_ids,
                         array_agg(DISTINCT p.id) FILTER (WHERE p.id IS NOT NULL)                       AS contributed_on_project_ids,
                         array_agg(DISTINCT p.slug) FILTER (WHERE p.slug IS NOT NULL)                   AS contributed_on_project_slugs,
                         array_agg(DISTINCT ppc.project_category_id)
                         FILTER (WHERE ppc.project_category_id IS NOT NULL)                             AS project_category_ids,
                         array_agg(DISTINCT lfe.language_id) FILTER (WHERE lfe.language_id IS NOT NULL) AS language_ids,
                         array_agg(DISTINCT pe.ecosystem_id)
                         FILTER (WHERE pe.ecosystem_id IS NOT NULL)                                     AS ecosystem_ids,
                         array_agg(DISTINCT pp.program_id) FILTER (WHERE pp.program_id IS NOT NULL)     AS program_ids
                  FROM indexer_exp.github_accounts ga
                           LEFT JOIN indexer_exp.repos_contributors rc ON rc.contributor_id = ga.id
                           LEFT JOIN project_github_repos pgr ON pgr.github_repo_id = rc.repo_id
                           LEFT JOIN projects p ON p.id = pgr.project_id
                           LEFT JOIN projects_ecosystems pe ON pe.project_id = p.id
                           LEFT JOIN m_programs_projects pp ON pp.project_id = p.id
                           LEFT JOIN projects_project_categories ppc ON ppc.project_id = p.id
                           LEFT JOIN indexer_exp.github_user_file_extensions gufe ON gufe.user_id = ga.id
                           LEFT JOIN language_file_extensions lfe ON lfe.extension = gufe.file_extension
                           LEFT JOIN iam.users u ON u.github_user_id = ga.id
                           LEFT JOIN project_leads pl ON pl.user_id = u.id
                  GROUP BY ga.id, u.id) c_1) c) v;

create materialized view if not exists bi.m_contributor_global_data as
SELECT v_contributor_global_data.contributor_id,
       v_contributor_global_data.contributor_login,
       v_contributor_global_data.contributor_user_id,
       v_contributor_global_data.first_project_name,
       v_contributor_global_data.maintained_project_ids,
       v_contributor_global_data.contributed_on_project_ids,
       v_contributor_global_data.contributed_on_project_slugs,
       v_contributor_global_data.project_category_ids,
       v_contributor_global_data.language_ids,
       v_contributor_global_data.ecosystem_ids,
       v_contributor_global_data.program_ids,
       v_contributor_global_data.contributor_country,
       v_contributor_global_data.contributor,
       v_contributor_global_data.maintained_projects,
       v_contributor_global_data.projects,
       v_contributor_global_data.languages,
       v_contributor_global_data.ecosystems,
       v_contributor_global_data.categories,
       v_contributor_global_data.programs,
       v_contributor_global_data.search,
       v_contributor_global_data.hash
FROM bi.v_contributor_global_data;

create unique index if not exists bi_m_contributor_global_data_pk
    on bi.m_contributor_global_data (contributor_id);

create unique index if not exists bi_m_contributor_global_data_contributor_id_hash_idx
    on bi.m_contributor_global_data (contributor_id, hash);

create or replace view bi.v_contribution_contributors_data
            (contribution_uuid, repo_id, github_author_id, contributor_ids, assignee_ids, applicant_ids, github_author,
             contributors, applicants, search, hash)
as
SELECT v.contribution_uuid,
       v.repo_id,
       v.github_author_id,
       v.contributor_ids,
       v.assignee_ids,
       v.applicant_ids,
       v.github_author,
       v.contributors,
       v.applicants,
       v.search,
       md5(v.*::text) AS hash
FROM (SELECT c.contribution_uuid,
             c.repo_id,
             c.github_author_id,
             array_agg(DISTINCT gcc.contributor_id) FILTER (WHERE gcc.contributor_id IS NOT NULL) AS contributor_ids,
             array_agg(DISTINCT gia.user_id) FILTER (WHERE gia.user_id IS NOT NULL)               AS assignee_ids,
             array_agg(DISTINCT a.applicant_id) FILTER (WHERE a.applicant_id IS NOT NULL)         AS applicant_ids,
             CASE
                 WHEN ad.contributor_id IS NOT NULL THEN ad.contributor
                 ELSE NULL::jsonb
                 END                                                                              AS github_author,
             jsonb_agg(DISTINCT
             jsonb_set(cd.contributor, '{since}'::text[], to_jsonb(gcc.tech_created_at::timestamp with time zone),
                       true)) FILTER (WHERE cd.contributor_id IS NOT NULL)                        AS contributors,
             jsonb_agg(DISTINCT jsonb_set(
                     jsonb_set(apd.contributor, '{since}'::text[], to_jsonb(a.received_at::timestamp with time zone),
                               true), '{applicationId}'::text[], to_jsonb(a.id), true))
             FILTER (WHERE apd.contributor_id IS NOT NULL)                                        AS applicants,
             concat(c.github_number, ' ', c.github_title, ' ', ad.contributor_login, ' ',
                    string_agg(DISTINCT cd.contributor_login, ' '::text), ' ',
                    string_agg(DISTINCT apd.contributor_login, ' '::text))                        AS search
      FROM indexer_exp.grouped_contributions c
               LEFT JOIN indexer_exp.grouped_contribution_contributors gcc
                         ON gcc.contribution_uuid = c.contribution_uuid
               LEFT JOIN bi.p_contributor_global_data cd ON cd.contributor_id = gcc.contributor_id
               LEFT JOIN bi.p_contributor_global_data ad ON ad.contributor_id = c.github_author_id
               LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = c.issue_id
               LEFT JOIN applications a ON a.issue_id = c.issue_id
               LEFT JOIN bi.p_contributor_global_data apd ON apd.contributor_id = a.applicant_id
      GROUP BY c.contribution_uuid, ad.contributor_id) v;

create materialized view if not exists bi.m_contribution_contributors_data as
SELECT v_contribution_contributors_data.contribution_uuid,
       v_contribution_contributors_data.repo_id,
       v_contribution_contributors_data.github_author_id,
       v_contribution_contributors_data.contributor_ids,
       v_contribution_contributors_data.assignee_ids,
       v_contribution_contributors_data.applicant_ids,
       v_contribution_contributors_data.github_author,
       v_contribution_contributors_data.contributors,
       v_contribution_contributors_data.applicants,
       v_contribution_contributors_data.search,
       v_contribution_contributors_data.hash
FROM bi.v_contribution_contributors_data;

create unique index if not exists bi_m_contribution_contributors_data_pk
    on bi.m_contribution_contributors_data (contribution_uuid);

create unique index if not exists bi_m_contribution_contributors_data_contribution_uuid_hash_idx
    on bi.m_contribution_contributors_data (contribution_uuid, hash);


create or replace view bi.weekly_rewards_creation_stats_per_currency
            (creation_date, currency, total_usd_amount, total_amount, rewards_count, contributors_count,
             project_leads_count)
as
SELECT date_trunc('week'::text, r.requested_at) AS creation_date,
       c.code                                   AS currency,
       sum(rsd.amount_usd_equivalent)           AS total_usd_amount,
       sum(r.amount)                            AS total_amount,
       count(DISTINCT r.id)                     AS rewards_count,
       count(DISTINCT r.recipient_id)           AS contributors_count,
       count(DISTINCT r.requestor_id)           AS project_leads_count
FROM public.rewards r
         JOIN public.currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
GROUP BY (date_trunc('week'::text, r.requested_at)), c.code;

create or replace view bi.monthly_rewards_creation_stats_per_currency
            (creation_date, currency, total_usd_amount, total_amount, rewards_count, contributors_count,
             project_leads_count)
as
SELECT date_trunc('month'::text, r.requested_at) AS creation_date,
       c.code                                    AS currency,
       sum(rsd.amount_usd_equivalent)            AS total_usd_amount,
       sum(r.amount)                             AS total_amount,
       count(DISTINCT r.id)                      AS rewards_count,
       count(DISTINCT r.recipient_id)            AS contributors_count,
       count(DISTINCT r.requestor_id)            AS project_leads_count
FROM public.rewards r
         JOIN public.currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
GROUP BY (date_trunc('month'::text, r.requested_at)), c.code;

create or replace view bi.weekly_users_creation_stats(creation_date, users_count) as
SELECT date_trunc('week'::text, u.created_at) AS creation_date,
       count(u.id)                            AS users_count
FROM iam.users u
GROUP BY (date_trunc('week'::text, u.created_at));

create or replace view bi.monthly_users_creation_stats(creation_date, users_count) as
SELECT date_trunc('month'::text, u.created_at) AS creation_date,
       count(u.id)                             AS users_count
FROM iam.users u
GROUP BY (date_trunc('month'::text, u.created_at));

create or replace view bi.weekly_projects_creation_stats(creation_date, projects_count) as
SELECT date_trunc('week'::text, p.created_at) AS creation_date,
       count(p.id)                            AS projects_count
FROM public.projects p
GROUP BY (date_trunc('week'::text, p.created_at));

create or replace view bi.monthly_projects_creation_stats(creation_date, projects_count) as
SELECT date_trunc('month'::text, p.created_at) AS creation_date,
       count(p.id)                             AS projects_count
FROM public.projects p
GROUP BY (date_trunc('month'::text, p.created_at));

create or replace view bi.weekly_contributions_creation_stats
    (creation_date, contributors_count, registered_contributors_count, external_contributors_count) as
SELECT date_trunc('week'::text, c.created_at)                           AS creation_date,
       count(DISTINCT c.contributor_id)                                 AS contributors_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE u.id IS NOT NULL) AS registered_contributors_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE u.id IS NULL)     AS external_contributors_count
FROM indexer_exp.contributions c
         LEFT JOIN iam.users u ON u.github_user_id = c.contributor_id
GROUP BY (date_trunc('week'::text, c.created_at));

create or replace view bi.monthly_contributions_creation_stats
    (creation_date, contributors_count, registered_contributors_count, external_contributors_count) as
SELECT date_trunc('month'::text, c.created_at)                          AS creation_date,
       count(DISTINCT c.contributor_id)                                 AS contributors_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE u.id IS NOT NULL) AS registered_contributors_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE u.id IS NULL)     AS external_contributors_count
FROM indexer_exp.contributions c
         LEFT JOIN iam.users u ON u.github_user_id = c.contributor_id
GROUP BY (date_trunc('month'::text, c.created_at));

create or replace view bi.daily_users_creation_stats(creation_date, users_count) as
SELECT date_trunc('day'::text, u.created_at) AS creation_date,
       count(u.id)                           AS users_count
FROM iam.users u
GROUP BY (date_trunc('day'::text, u.created_at));

create or replace view bi.daily_projects_creation_stats(creation_date, projects_count) as
SELECT date_trunc('day'::text, p.created_at) AS creation_date,
       count(p.id)                           AS projects_count
FROM public.projects p
GROUP BY (date_trunc('day'::text, p.created_at));

create or replace view bi.monthly_contributors(date, repo_id, contributor_id, first, latest) as
WITH monthly AS (SELECT generate_series(date_trunc('month'::text, '2023-01-01'::date::timestamp with time zone),
                                        date_trunc('month'::text, CURRENT_DATE::timestamp with time zone),
                                        '1 mon'::interval) AS date)
SELECT m.date,
       c.repo_id,
       c.contributor_id,
       date_trunc('month'::text, min(c.created_at)) AS first,
       date_trunc('month'::text, max(c.created_at)) AS latest
FROM monthly m
         LEFT JOIN indexer_exp.contributions c ON date_trunc('month'::text, c.created_at) <= m.date
GROUP BY m.date, c.repo_id, c.contributor_id;

create or replace view bi.monthly_contributions_stats_per_ecosystem
            (ecosystem, creation_date, contributors_count, new_contributors_count, churned_contributors_count) as
SELECT e.name                                                            AS ecosystem,
       c.date                                                            AS creation_date,
       count(DISTINCT c.contributor_id) FILTER (WHERE c.latest = c.date) AS contributors_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE c.first = c.date)  AS new_contributors_count,
       count(DISTINCT c.contributor_id)
       FILTER (WHERE c.latest = (c.date - '1 mon'::interval))            AS churned_contributors_count
FROM bi.monthly_contributors c
         JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id
         JOIN projects_ecosystems pe ON pe.project_id = pgr.project_id
         JOIN ecosystems e ON e.id = pe.ecosystem_id
GROUP BY e.name, c.date;

create or replace view bi.weekly_contributors(date, repo_id, contributor_id, first, latest) as
WITH weekly AS (SELECT generate_series(date_trunc('week'::text, '2023-01-01'::date::timestamp with time zone),
                                       date_trunc('week'::text, CURRENT_DATE::timestamp with time zone),
                                       '7 days'::interval) AS date)
SELECT w.date,
       c.repo_id,
       c.contributor_id,
       date_trunc('week'::text, min(c.created_at)) AS first,
       date_trunc('week'::text, max(c.created_at)) AS latest
FROM weekly w
         LEFT JOIN indexer_exp.contributions c ON date_trunc('week'::text, c.created_at) <= w.date
GROUP BY w.date, c.repo_id, c.contributor_id;

create or replace view bi.weekly_contributions_stats_per_ecosystem
            (ecosystem, creation_date, contributors_count, new_contributors_count, churned_contributors_count) as
SELECT e.name                                                            AS ecosystem,
       c.date                                                            AS creation_date,
       count(DISTINCT c.contributor_id) FILTER (WHERE c.latest = c.date) AS contributors_count,
       count(DISTINCT c.contributor_id) FILTER (WHERE c.first = c.date)  AS new_contributors_count,
       count(DISTINCT c.contributor_id)
       FILTER (WHERE c.latest = (c.date - '7 days'::interval))           AS churned_contributors_count
FROM bi.weekly_contributors c
         JOIN project_github_repos pgr ON pgr.github_repo_id = c.repo_id
         JOIN projects_ecosystems pe ON pe.project_id = pgr.project_id
         JOIN ecosystems e ON e.id = pe.ecosystem_id
GROUP BY e.name, c.date;

create or replace view bi.weekly_rewards_creation_stats
            (creation_date, total_usd_amount, rewards_count, contributors_count, new_contributors_count,
             project_leads_count)
as
WITH first_rewards AS (SELECT rewards.recipient_id,
                              date_trunc('week'::text, min(rewards.requested_at)) AS date
                       FROM rewards
                       GROUP BY rewards.recipient_id)
SELECT date_trunc('week'::text, r.requested_at)                          AS creation_date,
       sum(rsd.amount_usd_equivalent)                                    AS total_usd_amount,
       count(DISTINCT r.id)                                              AS rewards_count,
       count(DISTINCT r.recipient_id)                                    AS contributors_count,
       count(DISTINCT r.recipient_id)
       FILTER (WHERE fr.date = date_trunc('week'::text, r.requested_at)) AS new_contributors_count,
       count(DISTINCT r.requestor_id)                                    AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         JOIN first_rewards fr ON fr.recipient_id = r.recipient_id
GROUP BY (date_trunc('week'::text, r.requested_at));

create or replace view bi.monthly_rewards_creation_stats
            (creation_date, total_usd_amount, rewards_count, contributors_count, new_contributors_count,
             project_leads_count)
as
WITH first_rewards AS (SELECT rewards.recipient_id,
                              date_trunc('month'::text, min(rewards.requested_at)) AS date
                       FROM rewards
                       GROUP BY rewards.recipient_id)
SELECT date_trunc('month'::text, r.requested_at)                          AS creation_date,
       sum(rsd.amount_usd_equivalent)                                     AS total_usd_amount,
       count(DISTINCT r.id)                                               AS rewards_count,
       count(DISTINCT r.recipient_id)                                     AS contributors_count,
       count(DISTINCT r.recipient_id)
       FILTER (WHERE fr.date = date_trunc('month'::text, r.requested_at)) AS new_contributors_count,
       count(DISTINCT r.requestor_id)                                     AS project_leads_count
FROM rewards r
         JOIN currencies c ON c.id = r.currency_id
         JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
         JOIN first_rewards fr ON fr.recipient_id = r.recipient_id
GROUP BY (date_trunc('month'::text, r.requested_at));

create or replace view bi.sponsor_stats_per_currency
            (sponsor_id, currency_id, initial_allowance, total_allocated, total_granted, total_rewarded, total_paid,
             initial_balance, total_spent)
as
WITH virtual_stats AS (SELECT abt.sponsor_id,
                              abt.currency_id,
                              sum(abt.amount) FILTER (WHERE abt.type = 'MINT'::accounting.transaction_type AND
                                                            abt.program_id IS NULL)    AS minted,
                              sum(abt.amount) FILTER (WHERE abt.type = 'REFUND'::accounting.transaction_type AND
                                                            abt.program_id IS NULL)    AS refunded,
                              sum(abt.amount) FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND
                                                            abt.program_id IS NOT NULL AND
                                                            abt.project_id IS NULL)    AS allocated,
                              sum(abt.amount) FILTER (WHERE abt.type = 'REFUND'::accounting.transaction_type AND
                                                            abt.program_id IS NOT NULL AND
                                                            abt.project_id IS NULL)    AS unallocated,
                              sum(abt.amount) FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND
                                                            abt.project_id IS NOT NULL AND
                                                            abt.reward_id IS NULL)     AS granted,
                              sum(abt.amount) FILTER (WHERE abt.type = 'REFUND'::accounting.transaction_type AND
                                                            abt.project_id IS NOT NULL AND
                                                            abt.reward_id IS NULL)     AS ungranted,
                              sum(abt.amount) FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND
                                                            abt.reward_id IS NOT NULL AND
                                                            abt.payment_id IS NULL)    AS rewarded,
                              sum(abt.amount) FILTER (WHERE abt.type = 'REFUND'::accounting.transaction_type AND
                                                            abt.reward_id IS NOT NULL AND
                                                            abt.payment_id IS NULL)    AS canceled,
                              sum(abt.amount) FILTER (WHERE abt.type = 'BURN'::accounting.transaction_type AND
                                                            abt.reward_id IS NOT NULL) AS paid
                       FROM accounting.all_transactions abt
                       GROUP BY abt.sponsor_id, abt.currency_id),
     physical_stats AS (SELECT sa.sponsor_id,
                               sa.currency_id,
                               sum(sat.amount)
                               FILTER (WHERE sat.type = 'DEPOSIT'::accounting.transaction_type)  AS deposited,
                               sum(sat.amount)
                               FILTER (WHERE sat.type = 'WITHDRAW'::accounting.transaction_type) AS withdrawn,
                               sum(sat.amount)
                               FILTER (WHERE sat.type = 'SPEND'::accounting.transaction_type)    AS spent
                        FROM accounting.sponsor_account_transactions sat
                                 JOIN accounting.sponsor_accounts sa ON sa.id = sat.account_id
                        GROUP BY sa.sponsor_id, sa.currency_id)
SELECT COALESCE(vs.sponsor_id, ps.sponsor_id)                                    AS sponsor_id,
       COALESCE(vs.currency_id, ps.currency_id)                                  AS currency_id,
       COALESCE(vs.minted, 0::numeric) - COALESCE(vs.refunded, 0::numeric)       AS initial_allowance,
       COALESCE(vs.allocated, 0::numeric) - COALESCE(vs.unallocated, 0::numeric) AS total_allocated,
       COALESCE(vs.granted, 0::numeric) - COALESCE(vs.ungranted, 0::numeric)     AS total_granted,
       COALESCE(vs.rewarded, 0::numeric) - COALESCE(vs.canceled, 0::numeric)     AS total_rewarded,
       COALESCE(vs.paid, 0::numeric)                                             AS total_paid,
       COALESCE(ps.deposited, 0::numeric) - COALESCE(ps.withdrawn, 0::numeric)   AS initial_balance,
       COALESCE(ps.spent, 0::numeric)                                            AS total_spent
FROM virtual_stats vs
         FULL JOIN physical_stats ps ON vs.sponsor_id = ps.sponsor_id AND vs.currency_id = ps.currency_id;

create or replace view bi.project_stats_per_currency(project_id, currency_id, total_granted, total_rewarded) as
SELECT abt.project_id,
       abt.currency_id,
       COALESCE(sum(abt.amount)
                FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND abt.reward_id IS NULL),
                0::numeric) - COALESCE(sum(abt.amount)
                                       FILTER (WHERE abt.type = 'REFUND'::accounting.transaction_type AND
                                                     abt.reward_id IS NULL), 0::numeric)     AS total_granted,
       COALESCE(sum(abt.amount)
                FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND abt.reward_id IS NOT NULL),
                0::numeric) - COALESCE(sum(abt.amount)
                                       FILTER (WHERE abt.type = 'REFUND'::accounting.transaction_type AND
                                                     abt.reward_id IS NOT NULL), 0::numeric) AS total_rewarded
FROM accounting.all_transactions abt
WHERE abt.project_id IS NOT NULL
  AND abt.payment_id IS NULL
GROUP BY abt.project_id, abt.currency_id;

create or replace view bi.program_stats_per_currency_per_project
    (program_id, currency_id, project_id, total_granted, total_rewarded, reward_count) as
SELECT abt.program_id,
       abt.currency_id,
       abt.project_id,
       COALESCE(sum(abt.amount)
                FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND abt.reward_id IS NULL),
                0::numeric) - COALESCE(sum(abt.amount)
                                       FILTER (WHERE abt.type = 'REFUND'::accounting.transaction_type AND
                                                     abt.reward_id IS NULL), 0::numeric)     AS total_granted,
       COALESCE(sum(abt.amount)
                FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND abt.reward_id IS NOT NULL),
                0::numeric) - COALESCE(sum(abt.amount)
                                       FILTER (WHERE abt.type = 'REFUND'::accounting.transaction_type AND
                                                     abt.reward_id IS NOT NULL), 0::numeric) AS total_rewarded,
       COALESCE(count(DISTINCT abt.reward_id) FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type),
                0::bigint) -
       COALESCE(count(DISTINCT abt.reward_id) FILTER (WHERE abt.type = 'REFUND'::accounting.transaction_type),
                0::bigint)                                                                   AS reward_count
FROM accounting.all_transactions abt
WHERE abt.program_id IS NOT NULL
  AND abt.project_id IS NOT NULL
  AND abt.payment_id IS NULL
GROUP BY abt.program_id, abt.currency_id, abt.project_id;

create or replace view bi.program_stats_per_currency
    (program_id, currency_id, total_allocated, total_granted, total_rewarded) as
SELECT abt.program_id,
       abt.currency_id,
       COALESCE(sum(abt.amount)
                FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND abt.program_id IS NOT NULL AND
                              abt.project_id IS NULL), 0::numeric) - COALESCE(sum(abt.amount) FILTER (WHERE
           abt.type = 'REFUND'::accounting.transaction_type AND abt.program_id IS NOT NULL AND abt.project_id IS NULL),
                                                                              0::numeric) AS total_allocated,
       COALESCE(sum(abt.amount)
                FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND abt.project_id IS NOT NULL AND
                              abt.reward_id IS NULL), 0::numeric) - COALESCE(sum(abt.amount) FILTER (WHERE
           abt.type = 'REFUND'::accounting.transaction_type AND abt.project_id IS NOT NULL AND abt.reward_id IS NULL),
                                                                             0::numeric)  AS total_granted,
       COALESCE(sum(abt.amount)
                FILTER (WHERE abt.type = 'TRANSFER'::accounting.transaction_type AND abt.reward_id IS NOT NULL AND
                              abt.payment_id IS NULL), 0::numeric) - COALESCE(sum(abt.amount) FILTER (WHERE
           abt.type = 'REFUND'::accounting.transaction_type AND abt.reward_id IS NOT NULL AND abt.payment_id IS NULL),
                                                                              0::numeric) AS total_rewarded
FROM accounting.all_transactions abt
GROUP BY abt.program_id, abt.currency_id;

create or replace view bi.program_stats(program_id, granted_project_count, reward_count, user_count) as
WITH project_users AS (SELECT pc.project_id,
                              pc.github_user_id AS contributor_id
                       FROM projects_contributors pc
                       UNION
                       SELECT pl.project_id,
                              u.github_user_id AS contributor_id
                       FROM project_leads pl
                                JOIN iam.users u ON u.id = pl.user_id)
SELECT p.id                                                   AS program_id,
       COALESCE(count(DISTINCT s.project_id), 0::bigint)      AS granted_project_count,
       COALESCE(sum(s.reward_count)::bigint, 0::bigint)       AS reward_count,
       COALESCE(count(DISTINCT pu.contributor_id), 0::bigint) AS user_count
FROM programs p
         LEFT JOIN bi.program_stats_per_currency_per_project s ON p.id = s.program_id AND s.total_granted > 0::numeric
         LEFT JOIN project_users pu ON pu.project_id = s.project_id
GROUP BY p.id;

create or replace view bi.v_per_contributor_contribution_data
            (technical_id, contribution_uuid, repo_id, project_id, project_slug, contributor_id, contributor_user_id,
             contributor_country, timestamp, contribution_status, contribution_type, github_author_id, github_number,
             github_status, github_title, github_html_url, github_body, created_at, updated_at, completed_at,
             day_timestamp, week_timestamp, month_timestamp, quarter_timestamp, year_timestamp,
             is_first_contribution_on_onlydust, is_issue, is_pr, is_code_review, activity_status, language_ids,
             ecosystem_ids, program_ids, project_category_ids, languages, is_good_first_issue, assignee_ids,
             github_label_ids, closing_issue_ids, applicant_ids, hash)
as
SELECT v.technical_id,
       v.contribution_uuid,
       v.repo_id,
       v.project_id,
       v.project_slug,
       v.contributor_id,
       v.contributor_user_id,
       v.contributor_country,
       v."timestamp",
       v.contribution_status,
       v.contribution_type,
       v.github_author_id,
       v.github_number,
       v.github_status,
       v.github_title,
       v.github_html_url,
       v.github_body,
       v.created_at,
       v.updated_at,
       v.completed_at,
       v.day_timestamp,
       v.week_timestamp,
       v.month_timestamp,
       v.quarter_timestamp,
       v.year_timestamp,
       v.is_first_contribution_on_onlydust,
       v.is_issue,
       v.is_pr,
       v.is_code_review,
       v.activity_status,
       v.language_ids,
       v.ecosystem_ids,
       v.program_ids,
       v.project_category_ids,
       v.languages,
       v.is_good_first_issue,
       v.assignee_ids,
       v.github_label_ids,
       v.closing_issue_ids,
       v.applicant_ids,
       md5(v.*::text) AS hash
FROM (SELECT md5(ROW (c.contribution_uuid, cd.contributor_id)::text)::uuid      AS technical_id,
             c.contribution_uuid,
             c.repo_id,
             c.project_id,
             c.project_slug,
             cd.contributor_id,
             u.id                                                               AS contributor_user_id,
             (array_agg(kyc.country) FILTER (WHERE kyc.country IS NOT NULL))[1] AS contributor_country,
             c.created_at                                                       AS "timestamp",
             c.contribution_status,
             c.contribution_type,
             c.github_author_id,
             c.github_number,
             c.github_status,
             c.github_title,
             c.github_html_url,
             c.github_body,
             c.created_at,
             c.updated_at,
             c.completed_at,
             date_trunc('day'::text, c.created_at)                              AS day_timestamp,
             date_trunc('week'::text, c.created_at)                             AS week_timestamp,
             date_trunc('month'::text, c.created_at)                            AS month_timestamp,
             date_trunc('quarter'::text, c.created_at)                          AS quarter_timestamp,
             date_trunc('year'::text, c.created_at)                             AS year_timestamp,
             NOT (EXISTS (SELECT 1
                          FROM indexer_exp.contributions fc
                                   JOIN indexer_exp.github_repos gr ON gr.id = fc.repo_id
                                   JOIN project_github_repos pgr ON pgr.github_repo_id = gr.id
                          WHERE fc.contributor_id = cd.contributor_id
                            AND fc.created_at < c.created_at))                  AS is_first_contribution_on_onlydust,
             c.is_issue,
             c.is_pr,
             c.is_code_review,
             c.activity_status,
             c.language_ids,
             c.ecosystem_ids,
             c.program_ids,
             c.project_category_ids,
             c.languages,
             c.is_good_first_issue,
             ccd.assignee_ids,
             c.github_label_ids,
             c.closing_issue_ids,
             ccd.applicant_ids
      FROM bi.p_contribution_data c
               JOIN bi.p_contribution_contributors_data ccd ON c.contribution_uuid = ccd.contribution_uuid
               CROSS JOIN LATERAL unnest(ccd.contributor_ids) cd(contributor_id)
               LEFT JOIN iam.users u ON u.github_user_id = cd.contributor_id
               LEFT JOIN accounting.billing_profiles_users bpu ON bpu.user_id = u.id
               LEFT JOIN accounting.kyc ON kyc.billing_profile_id = bpu.billing_profile_id
      GROUP BY c.contribution_uuid, ccd.contribution_uuid, cd.contributor_id, u.id) v;

create or replace view bi.v_project_contributions_data(project_id, repo_ids, contributor_count, good_first_issue_count, hash) as
SELECT v.project_id,
       v.repo_ids,
       v.contributor_count,
       v.good_first_issue_count,
       md5(v.*::text) AS hash
FROM (SELECT p.id                                                        AS project_id,
             array_agg(DISTINCT cd.repo_id)                              AS repo_ids,
             count(DISTINCT cd.contributor_id)                           AS contributor_count,
             count(DISTINCT cd.contribution_uuid)
             FILTER (WHERE cd.is_good_first_issue AND COALESCE(array_length(cd.assignee_ids, 1), 0) = 0 AND
                           cd.contribution_status <> 'COMPLETED'::indexer_exp.contribution_status AND
                           cd.contribution_status <>
                           'CANCELLED'::indexer_exp.contribution_status) AS good_first_issue_count
      FROM projects p
               LEFT JOIN bi.p_per_contributor_contribution_data cd ON cd.project_id = p.id
      GROUP BY p.id) v;

create or replace view bi.v_application_data
            (application_id, contribution_uuid, timestamp, day_timestamp, week_timestamp, month_timestamp,
             quarter_timestamp, year_timestamp, contributor_id, origin, status, project_id, project_slug, repo_id,
             ecosystem_ids, program_ids, language_ids, project_category_ids, search, hash)
as
SELECT v.application_id,
       v.contribution_uuid,
       v."timestamp",
       v.day_timestamp,
       v.week_timestamp,
       v.month_timestamp,
       v.quarter_timestamp,
       v.year_timestamp,
       v.contributor_id,
       v.origin,
       v.status,
       v.project_id,
       v.project_slug,
       v.repo_id,
       v.ecosystem_ids,
       v.program_ids,
       v.language_ids,
       v.project_category_ids,
       v.search,
       md5(v.*::text) AS hash
FROM (SELECT a.id                                       AS application_id,
             cd.contribution_uuid,
             a.received_at                              AS "timestamp",
             date_trunc('day'::text, a.received_at)     AS day_timestamp,
             date_trunc('week'::text, a.received_at)    AS week_timestamp,
             date_trunc('month'::text, a.received_at)   AS month_timestamp,
             date_trunc('quarter'::text, a.received_at) AS quarter_timestamp,
             date_trunc('year'::text, a.received_at)    AS year_timestamp,
             a.applicant_id                             AS contributor_id,
             a.origin,
             CASE
                 WHEN ARRAY [a.applicant_id] <@ ccd.assignee_ids THEN 'ACCEPTED'::application_status
                 WHEN array_length(ccd.assignee_ids, 1) > 0 THEN 'SHELVED'::application_status
                 ELSE 'PENDING'::application_status
                 END                                    AS status,
             cd.project_id,
             cd.project_slug,
             cd.repo_id,
             cd.ecosystem_ids,
             cd.program_ids,
             cd.language_ids,
             cd.project_category_ids,
             concat(cd.search, ' ', iu.login)           AS search
      FROM applications a
               JOIN bi.p_contribution_data cd ON cd.issue_id = a.issue_id
               JOIN bi.p_contribution_contributors_data ccd ON ccd.contribution_uuid = cd.contribution_uuid
               LEFT JOIN iam.all_indexed_users iu ON iu.github_user_id = a.applicant_id
      GROUP BY a.id, cd.contribution_uuid, ccd.contribution_uuid, iu.login) v;


create materialized view if not exists bi.m_per_contributor_contribution_data as
SELECT v_per_contributor_contribution_data.technical_id,
       v_per_contributor_contribution_data.contribution_uuid,
       v_per_contributor_contribution_data.repo_id,
       v_per_contributor_contribution_data.project_id,
       v_per_contributor_contribution_data.project_slug,
       v_per_contributor_contribution_data.contributor_id,
       v_per_contributor_contribution_data.contributor_user_id,
       v_per_contributor_contribution_data.contributor_country,
       v_per_contributor_contribution_data."timestamp",
       v_per_contributor_contribution_data.contribution_status,
       v_per_contributor_contribution_data.contribution_type,
       v_per_contributor_contribution_data.github_author_id,
       v_per_contributor_contribution_data.github_number,
       v_per_contributor_contribution_data.github_status,
       v_per_contributor_contribution_data.github_title,
       v_per_contributor_contribution_data.github_html_url,
       v_per_contributor_contribution_data.github_body,
       v_per_contributor_contribution_data.created_at,
       v_per_contributor_contribution_data.updated_at,
       v_per_contributor_contribution_data.completed_at,
       v_per_contributor_contribution_data.day_timestamp,
       v_per_contributor_contribution_data.week_timestamp,
       v_per_contributor_contribution_data.month_timestamp,
       v_per_contributor_contribution_data.quarter_timestamp,
       v_per_contributor_contribution_data.year_timestamp,
       v_per_contributor_contribution_data.is_first_contribution_on_onlydust,
       v_per_contributor_contribution_data.is_issue,
       v_per_contributor_contribution_data.is_pr,
       v_per_contributor_contribution_data.is_code_review,
       v_per_contributor_contribution_data.activity_status,
       v_per_contributor_contribution_data.language_ids,
       v_per_contributor_contribution_data.ecosystem_ids,
       v_per_contributor_contribution_data.program_ids,
       v_per_contributor_contribution_data.project_category_ids,
       v_per_contributor_contribution_data.languages,
       v_per_contributor_contribution_data.is_good_first_issue,
       v_per_contributor_contribution_data.assignee_ids,
       v_per_contributor_contribution_data.github_label_ids,
       v_per_contributor_contribution_data.closing_issue_ids,
       v_per_contributor_contribution_data.applicant_ids,
       v_per_contributor_contribution_data.hash
FROM bi.v_per_contributor_contribution_data;

create unique index if not exists bi_m_per_contributor_contribution_data_pk
    on bi.m_per_contributor_contribution_data (technical_id);

create unique index if not exists bi_m_per_contributor_contribution_data_technical_id_hash_idx
    on bi.m_per_contributor_contribution_data (technical_id, hash);

create materialized view if not exists bi.m_project_contributions_data as
SELECT v_project_contributions_data.project_id,
       v_project_contributions_data.repo_ids,
       v_project_contributions_data.contributor_count,
       v_project_contributions_data.good_first_issue_count,
       v_project_contributions_data.hash
FROM bi.v_project_contributions_data;

create unique index if not exists bi_m_project_contributions_data_pk
    on bi.m_project_contributions_data (project_id);

create unique index if not exists bi_m_project_contributions_data_project_id_hash_idx
    on bi.m_project_contributions_data (project_id, hash);

create materialized view if not exists bi.m_application_data as
SELECT v_application_data.application_id,
       v_application_data.contribution_uuid,
       v_application_data."timestamp",
       v_application_data.day_timestamp,
       v_application_data.week_timestamp,
       v_application_data.month_timestamp,
       v_application_data.quarter_timestamp,
       v_application_data.year_timestamp,
       v_application_data.contributor_id,
       v_application_data.origin,
       v_application_data.status,
       v_application_data.project_id,
       v_application_data.project_slug,
       v_application_data.repo_id,
       v_application_data.ecosystem_ids,
       v_application_data.program_ids,
       v_application_data.language_ids,
       v_application_data.project_category_ids,
       v_application_data.search,
       v_application_data.hash
FROM bi.v_application_data;

create unique index if not exists bi_m_application_data_pk
    on bi.m_application_data (application_id);

create unique index if not exists bi_m_application_data_application_id_hash_idx
    on bi.m_application_data (application_id, hash);

create or replace function bi.select_projects(fromdate timestamp with time zone, todate timestamp with time zone, datasourceids uuid[], programids uuid[],
                                              projectids uuid[], projectslugs text[], projectleadids uuid[], categoryids uuid[], languageids uuid[],
                                              ecosystemids uuid[], searchquery text, showfilteredkpis boolean)
    returns TABLE
            (
                project_id                   uuid,
                project_name                 text,
                project                      jsonb,
                leads                        jsonb,
                categories                   jsonb,
                languages                    jsonb,
                ecosystems                   jsonb,
                programs                     jsonb,
                budget                       jsonb,
                available_budget_usd         numeric,
                percent_spent_budget_usd     numeric,
                total_granted_usd_amount     numeric,
                reward_count                 bigint,
                completed_issue_count        bigint,
                completed_pr_count           bigint,
                completed_code_review_count  bigint,
                completed_contribution_count bigint,
                total_rewarded_usd_amount    numeric,
                average_reward_usd_amount    numeric,
                active_contributor_count     bigint,
                onboarded_contributor_count  bigint
            )
    stable
    parallel restricted
    language sql
as
$$
SELECT p.project_id                         as project_id,
       p.project_name                       as project_name,
       p.project                            as project,
       p.leads                              as leads,
       p.categories                         as categories,
       p.languages                          as languages,
       p.ecosystems                         as ecosystems,
       p.programs                           as programs,
       pb.budget                            as budget,
       pb.available_budget_usd              as available_budget_usd,
       pb.percent_spent_budget_usd          as percent_spent_budget_usd,
       sum(gd.total_granted_usd_amount)     as total_granted_usd_amount,
       sum(rd.reward_count)                 as reward_count,
       sum(cd.completed_issue_count)        as completed_issue_count,
       sum(cd.completed_pr_count)           as completed_pr_count,
       sum(cd.completed_code_review_count)  as completed_code_review_count,
       sum(cd.completed_contribution_count) as completed_contribution_count,
       sum(rd.total_rewarded_usd_amount)    as total_rewarded_usd_amount,
       sum(rd.average_reward_usd_amount)    as average_reward_usd_amount,
       sum(cd.active_contributor_count)     as active_contributor_count,
       sum(cd.onboarded_contributor_count)  as onboarded_contributor_count
FROM bi.p_project_global_data p
         JOIN bi.p_project_budget_data pb on pb.project_id = p.project_id

         LEFT JOIN (select cd.project_id,
                           count(cd.contribution_uuid)                           as completed_contribution_count,
                           coalesce(sum(cd.is_issue), 0)                         as completed_issue_count,
                           coalesce(sum(cd.is_pr), 0)                            as completed_pr_count,
                           coalesce(sum(cd.is_code_review), 0)                   as completed_code_review_count,
                           count(distinct cd.contributor_id)                     as active_contributor_count,
                           count(distinct cd.contributor_id)
                           filter ( where cd.is_first_contribution_on_onlydust ) as onboarded_contributor_count
                    from bi.p_per_contributor_contribution_data cd
                    where cd.timestamp >= fromDate
                      and cd.timestamp < toDate
                      and (not showFilteredKpis or languageIds is null or cd.language_ids && languageIds)
                      and cd.contribution_status = 'COMPLETED'
                    group by cd.project_id) cd
                   on cd.project_id = p.project_id

         LEFT JOIN (select rd.project_id,
                           count(rd.reward_id)             as reward_count,
                           coalesce(sum(rd.usd_amount), 0) as total_rewarded_usd_amount,
                           coalesce(avg(rd.usd_amount), 0) as average_reward_usd_amount
                    from bi.p_reward_data rd
                    where rd.timestamp >= fromDate
                      and rd.timestamp < toDate
                      and (not showFilteredKpis or projectLeadIds is null or rd.requestor_id = any (projectLeadIds))
                      and (not showFilteredKpis or languageIds is null or rd.language_ids && languageIds)
                    group by rd.project_id) rd on rd.project_id = p.project_id

         LEFT JOIN (select gd.project_id,
                           coalesce(sum(gd.usd_amount), 0) as total_granted_usd_amount
                    from bi.p_project_grants_data gd
                    where gd.timestamp >= fromDate
                      and gd.timestamp < toDate
                    group by gd.project_id) gd on gd.project_id = p.project_id

WHERE (p.project_id = any (dataSourceIds) or p.program_ids && dataSourceIds or p.ecosystem_ids && dataSourceIds)
  and (ecosystemIds is null or p.ecosystem_ids && ecosystemIds)
  and (programIds is null or p.program_ids && programIds)
  and (projectIds is null or p.project_id = any (projectIds))
  and (projectSlugs is null or p.project_slug = any (projectSlugs))
  and (projectLeadIds is null or p.project_lead_ids && projectLeadIds)
  and (categoryIds is null or p.project_category_ids && categoryIds)
  and (languageIds is null or p.language_ids && languageIds)
  and (searchQuery is null or p.search ilike '%' || searchQuery || '%')
  and (cd.project_id is not null or rd.project_id is not null or gd.project_id is not null)

GROUP BY p.project_id, p.project_name, p.project, p.programs, p.ecosystems, p.languages, p.categories,
         p.leads, pb.budget, pb.available_budget_usd, pb.percent_spent_budget_usd
$$;
