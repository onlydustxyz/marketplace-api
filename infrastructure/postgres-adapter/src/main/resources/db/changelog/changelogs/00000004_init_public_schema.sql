create type allocated_time as enum ('none', 'less_than_one_day', 'one_to_three_days', 'greater_than_three_days');

create type billing_profile_type as enum ('INDIVIDUAL', 'COMPANY');

create type contribution_status as enum ('in_progress', 'complete', 'canceled');

create type contribution_type as enum ('ISSUE', 'PULL_REQUEST', 'CODE_REVIEW');

create type currency as enum ('usd', 'eth', 'op', 'apt', 'strk', 'lords', 'usdc');

create type currency_type as enum ('FIAT', 'CRYPTO');

create type github_ci_checks as enum ('passed', 'failed');

create type github_code_review_outcome as enum ('change_requested', 'approved');

create type github_code_review_status as enum ('pending', 'completed');

create type github_issue_status as enum ('open', 'completed', 'cancelled');

create type github_pull_request_status as enum ('open', 'closed', 'merged');

create type id_document_type as enum ('PASSPORT', 'ID_CARD', 'RESIDENCE_PERMIT', 'DRIVER_LICENSE');

create type outbox_event_status as enum ('PENDING', 'PROCESSED', 'FAILED', 'SKIPPED');

create type preferred_method as enum ('fiat', 'crypto');

create type profile_cover as enum ('cyan', 'magenta', 'yellow', 'blue');

create type project_tag as enum ('HOT_COMMUNITY', 'NEWBIES_WELCOME', 'LIKELY_TO_REWARD', 'WORK_IN_PROGRESS', 'FAST_AND_FURIOUS', 'BIG_WHALE', 'UPDATED_ROADMAP', 'HAS_GOOD_FIRST_ISSUES');

create type project_visibility as enum ('PUBLIC', 'PRIVATE');

create type verification_status as enum ('NOT_STARTED', 'STARTED', 'UNDER_REVIEW', 'VERIFIED', 'REJECTED', 'INVALIDATED', 'CLOSED');

create type wallet_type as enum ('address', 'name');

create type hackathon_status as enum ('DRAFT', 'PUBLISHED');

create type project_category_status as enum ('SUGGESTED', 'APPROVED');

create type committee_status as enum ('DRAFT', 'OPEN_TO_APPLICATIONS', 'OPEN_TO_VOTES', 'CLOSED');

create type banner_font_color as enum ('DARK', 'LIGHT');

create type activity_type as enum ('PULL_REQUEST', 'REWARD_CREATED', 'REWARD_CLAIMED', 'PROJECT_CREATED');

create type application_origin as enum ('GITHUB', 'MARKETPLACE');

create type contact_channel as enum ('TELEGRAM', 'TWITTER', 'DISCORD', 'LINKEDIN', 'WHATSAPP');

create type user_joining_reasons as enum ('CONTRIBUTOR', 'MAINTAINER');

create type user_joining_goals as enum ('LEARN', 'CHALLENGE', 'EARN', 'NOTORIETY');

create type activity_status as enum ('ARCHIVED', 'NOT_ASSIGNED', 'IN_PROGRESS', 'TO_REVIEW', 'DONE');

create type application_status as enum ('PENDING', 'ACCEPTED', 'SHELVED');

create type contributor_activity_status as enum ('NEW', 'REACTIVATED', 'CHURNED', 'ACTIVE', 'INACTIVE');

create table if not exists currencies
(
    id                   uuid                           not null
        primary key,
    type                 currency_type                  not null,
    name                 text                           not null,
    code                 text                           not null,
    logo_url             text,
    decimals             integer                        not null,
    description          text,
    tech_created_at      timestamptz default now()        not null,
    tech_updated_at      timestamptz default now()        not null,
    country_restrictions text[]    default '{}'::text[] not null,
    cmc_id               integer                        not null
);

create unique index if not exists currency_code_idx
    on currencies (code);

create unique index if not exists currencies_id_country_restrictions_idx
    on currencies (id, country_restrictions);

create table if not exists contact_informations
(
    user_id uuid            not null,
    channel contact_channel not null,
    contact text            not null,
    public  boolean         not null,
    primary key (user_id, channel)
);

create table if not exists custom_ignored_contributions
(
    project_id      uuid    not null,
    contribution_id text    not null,
    ignored         boolean not null,
    primary key (project_id, contribution_id)
);

create table if not exists global_settings
(
    id                                       serial
        primary key,
    terms_and_conditions_latest_version_date timestamptz not null,
    invoice_mandate_latest_version_date      timestamptz not null,
    required_github_app_permissions          text[]    not null
);

create table if not exists hidden_contributors
(
    project_id                 uuid    not null,
    project_lead_id            uuid    not null,
    contributor_github_user_id integer not null,
    primary key (project_id, project_lead_id, contributor_github_user_id)
);

create table if not exists ignored_contributions
(
    project_id      uuid not null,
    contribution_id text not null,
    primary key (project_id, contribution_id)
);

create table if not exists indexer_outbox_events
(
    id              bigserial
        primary key,
    payload         jsonb                                                      not null,
    status          outbox_event_status default 'PENDING'::outbox_event_status not null,
    error           text,
    tech_created_at timestamptz           default now()                          not null,
    tech_updated_at timestamptz           default now()                          not null
);

create table if not exists notification_outbox_events
(
    id              bigserial
        constraint notifications_pkey
            primary key,
    payload         jsonb                                                      not null,
    status          outbox_event_status default 'PENDING'::outbox_event_status not null,
    error           text,
    tech_created_at timestamptz           default now()                          not null,
    tech_updated_at timestamptz           default now()                          not null
);

create table if not exists onboardings
(
    user_id                              uuid not null
        constraint terms_and_conditions_acceptances_pkey
            primary key,
    terms_and_conditions_acceptance_date timestamptz,
    completion_date                      timestamptz
);

create table if not exists projects
(
    id                                                 uuid                       not null
        constraint project_details_pkey
            primary key,
    telegram_link                                      text,
    logo_url                                           text,
    name                                               text      default ''::text not null,
    short_description                                  text      default ''::text not null,
    long_description                                   text      default ''::text not null,
    hiring                                             boolean   default false    not null,
    rank                                               integer   default 0        not null,
    visibility                                         project_visibility         not null,
    reward_ignore_pull_requests_by_default             boolean   default false    not null,
    reward_ignore_issues_by_default                    boolean   default false    not null,
    reward_ignore_code_reviews_by_default              boolean   default false    not null,
    reward_ignore_contributions_before_date_by_default timestamptz,
    created_at                                         timestamptz default now()    not null,
    updated_at                                         timestamptz,
    slug                                               text                       not null,
    bot_notify_external_applications                   boolean                    not null
);

create table if not exists pending_project_leader_invitations
(
    id             uuid default gen_random_uuid() not null
        primary key,
    project_id     uuid                           not null
        references projects,
    github_user_id bigint                         not null,
    constraint pending_project_leader_invitation_project_id_github_user_id_key
        unique (project_id, github_user_id)
);

create table if not exists project_allowances
(
    project_id        uuid                    not null
        references projects,
    currency_id       uuid                    not null
        references currencies,
    current_allowance numeric                 not null,
    initial_allowance numeric                 not null,
    tech_created_at   timestamptz default now() not null,
    tech_updated_at   timestamptz default now() not null,
    primary key (project_id, currency_id)
);

create index if not exists project_details_name_idx
    on projects (name);

create index if not exists project_details_name_trgm_idx
    on projects using gin (name gin_trgm_ops);

create index if not exists project_details_rank_idx
    on projects (rank desc);

create index if not exists project_details_short_description_trgm_idx
    on projects using gin (short_description gin_trgm_ops);

create unique index if not exists projects_slug
    on projects (slug);

create table if not exists project_github_repos
(
    project_id     uuid   not null,
    github_repo_id bigint not null,
    primary key (project_id, github_repo_id)
);

create unique index if not exists project_github_repos_github_repo_id_project_id_idx
    on project_github_repos (github_repo_id, project_id);

create table if not exists project_leads
(
    project_id  uuid                    not null,
    user_id     uuid                    not null,
    assigned_at timestamptz default now() not null,
    primary key (project_id, user_id)
);

create unique index if not exists project_leads_user_id_project_id_idx
    on project_leads (user_id, project_id);

create table if not exists project_more_infos
(
    project_id uuid                    not null,
    url        text                    not null,
    name       text,
    created_at timestamptz default now() not null,
    updated_at timestamptz,
    rank       integer                 not null
);

create table if not exists projects_tags
(
    project_id uuid                    not null,
    tag        project_tag             not null,
    created_at timestamptz default now() not null,
    updated_at timestamptz default now() not null
);

create table if not exists sponsors
(
    id       uuid default gen_random_uuid() not null
        primary key,
    name     text                           not null
        unique,
    logo_url text                           not null,
    url      text
);

create table if not exists technologies
(
    technology text not null
        primary key
);

create table if not exists tracking_outbox_events
(
    id              bigserial
        primary key,
    payload         jsonb                                                      not null,
    status          outbox_event_status default 'PENDING'::outbox_event_status not null,
    error           text,
    tech_created_at timestamptz           default now()                          not null,
    tech_updated_at timestamptz           default now()                          not null
);

create table if not exists user_profile_info
(
    id                     uuid                               not null
        primary key,
    bio                    text,
    location               text,
    website                text,
    languages              jsonb,
    weekly_allocated_time  allocated_time,
    looking_for_a_job      boolean,
    avatar_url             text,
    cover                  profile_cover,
    tech_created_at        timestamptz            default now() not null,
    tech_updated_at        timestamptz            default now() not null,
    first_name             text,
    last_name              text,
    joining_reason         user_joining_reasons default 'CONTRIBUTOR'::user_joining_reasons,
    joining_goal           user_joining_goals,
    preferred_language_ids uuid[],
    preferred_category_ids uuid[],
    contact_email          text
);

create table if not exists hackathons
(
    id              uuid                           not null
        primary key,
    slug            text                           not null,
    status          hackathon_status               not null,
    title           text                           not null,
    description     text,
    location        text,
    budget          text,
    start_date      timestamptz                      not null,
    end_date        timestamptz                      not null,
    links           jsonb                          not null,
    tech_created_at timestamptz default now()        not null,
    tech_updated_at timestamptz default now()        not null,
    index           serial,
    github_labels   text[]    default '{}'::text[] not null,
    community_links jsonb     default '[]'::jsonb  not null
);

create unique index if not exists hackathons_slug_uindex
    on hackathons (slug);

create index if not exists hackathons_github_labels_idx
    on hackathons using gin (github_labels);

create table if not exists node_guardians_boost_rewards
(
    boosted_reward_id uuid                    not null,
    boost_reward_id   uuid,
    recipient_id      bigint                  not null,
    tech_created_at   timestamptz default now() not null,
    tech_updated_at   timestamptz default now() not null,
    primary key (recipient_id, boosted_reward_id)
);

create table if not exists node_guardians_rewards_boost_outbox_events
(
    id              bigserial
        primary key,
    payload         jsonb                                                      not null,
    status          outbox_event_status default 'PENDING'::outbox_event_status not null,
    error           text,
    tech_created_at timestamptz           default now()                          not null,
    tech_updated_at timestamptz           default now()                          not null
);

create table if not exists languages
(
    id              uuid                    not null
        primary key,
    name            text                    not null,
    logo_url        text,
    banner_url      text,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    slug            text                    not null
        unique
);

create table if not exists language_file_extensions
(
    extension       text                    not null
        primary key,
    language_id     uuid                    not null
        references languages
            on delete cascade,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null
);

create unique index if not exists language_file_extensions_extension_language_id_key
    on language_file_extensions (extension, language_id);

create unique index if not exists language_file_extensions_language_id_extension_key
    on language_file_extensions (language_id, extension);

create table if not exists historical_user_ranks
(
    github_user_id bigint    not null,
    rank           integer   not null,
    timestamp      timestamptz not null,
    primary key (github_user_id, timestamp)
);

create table if not exists project_categories
(
    id              uuid                    not null
        primary key,
    name            text                    not null,
    icon_slug       text                    not null,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    slug            text                    not null
        unique,
    description     text                    not null
);

create table if not exists committees
(
    id                     uuid                    not null
        primary key,
    status                 committee_status        not null,
    name                   text                    not null,
    application_start_date timestamptz               not null,
    application_end_date   timestamptz               not null,
    tech_created_at        timestamptz default now() not null,
    tech_updated_at        timestamptz default now() not null,
    vote_per_jury          integer
);

create table if not exists committee_project_questions
(
    id              uuid                    not null
        primary key,
    committee_id    uuid                    not null
        references committees,
    question        text                    not null,
    required        boolean                 not null,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    rank            integer
);

create index if not exists idx_committee_project_questions_committee_id_rank
    on committee_project_questions (committee_id, rank);

create table if not exists committee_project_answers
(
    committee_id    uuid                    not null
        references committees,
    project_id      uuid                    not null
        references projects,
    question_id     uuid                    not null
        references committee_project_questions,
    user_id         uuid                    not null,
    answer          text,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    primary key (committee_id, project_id, question_id)
);

create table if not exists mail_outbox_events
(
    id              bigserial
        primary key,
    payload         jsonb                                                      not null,
    status          outbox_event_status default 'PENDING'::outbox_event_status not null,
    error           text,
    tech_created_at timestamptz           default now()                          not null,
    tech_updated_at timestamptz           default now()                          not null
);

create table if not exists committee_jury_criteria
(
    id              uuid                    not null
        primary key,
    committee_id    uuid                    not null
        references committees,
    criteria        text                    not null,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    rank            integer
);

create index if not exists idx_committee_jury_criteria_committee_id_rank
    on committee_jury_criteria (committee_id, rank);

create table if not exists committee_budget_allocations
(
    committee_id    uuid    not null
        references committees,
    project_id      uuid    not null
        references projects,
    currency_id     uuid    not null
        references currencies,
    amount          numeric not null,
    tech_created_at timestamptz default now(),
    tech_updated_at timestamptz default now(),
    primary key (committee_id, currency_id, project_id)
);

create table if not exists ecosystem_banners
(
    id              uuid                    not null
        primary key,
    font_color      banner_font_color       not null,
    image_url       text                    not null,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null
);

create table if not exists ecosystems
(
    id              uuid      default gen_random_uuid() not null
        primary key,
    name            text                                not null
        unique,
    logo_url        text                                not null,
    url             text,
    banner_url      text,
    slug            text                                not null,
    description     text,
    md_banner_id    uuid
        references ecosystem_banners,
    xl_banner_id    uuid
        references ecosystem_banners,
    featured_rank   integer,
    tech_created_at timestamptz default now()             not null,
    tech_updated_at timestamptz default now()             not null,
    hidden          boolean                             not null
);

create table if not exists projects_ecosystems
(
    project_id    uuid not null
        references projects,
    ecosystem_id  uuid not null
        references ecosystems
            on delete cascade,
    featured_rank integer,
    primary key (project_id, ecosystem_id)
);

create table if not exists ecosystem_articles
(
    id              uuid                    not null
        primary key,
    title           text                    not null,
    description     text                    not null,
    url             text                    not null,
    image_url       text                    not null,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null
);

create table if not exists ecosystems_articles
(
    ecosystem_id    uuid                    not null
        references ecosystems,
    article_id      uuid                    not null
        references ecosystem_articles,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    primary key (ecosystem_id, article_id)
);

create table if not exists project_category_suggestions
(
    id              uuid                    not null
        primary key,
    name            text                    not null,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    project_id      uuid                    not null
        references projects
);

create table if not exists projects_project_categories
(
    project_id          uuid                    not null
        references projects,
    project_category_id uuid                    not null
        references project_categories,
    tech_created_at     timestamptz default now() not null,
    tech_updated_at     timestamptz default now() not null,
    primary key (project_id, project_category_id)
);

create table if not exists banners
(
    id                uuid                    not null
        primary key,
    short_description text                    not null,
    button_text       text,
    button_icon_slug  text,
    button_link_url   text,
    visible           boolean                 not null,
    updated_at        timestamptz               not null,
    tech_created_at   timestamptz default now() not null,
    tech_updated_at   timestamptz default now() not null,
    long_description  text                    not null,
    title             text                    not null,
    sub_title         text                    not null,
    date              timestamptz
);

create index if not exists banners_visible_index
    on banners (visible);

create table if not exists hackathon_projects
(
    hackathon_id    uuid                    not null
        references hackathons,
    project_id      uuid                    not null
        references projects,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    primary key (hackathon_id, project_id)
);

create table if not exists hackathon_events
(
    id              uuid                          not null
        primary key,
    hackathon_id    uuid                          not null
        references hackathons,
    name            text                          not null,
    subtitle        text                          not null,
    icon_slug       text                          not null,
    start_at        timestamptz                     not null,
    end_at          timestamptz                     not null,
    links           jsonb     default '[]'::jsonb not null,
    tech_created_at timestamptz default now()       not null,
    tech_updated_at timestamptz default now()       not null
);

create table if not exists programs
(
    id              uuid                    not null
        primary key,
    name            text                    not null,
    url             text,
    logo_url        text,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    sponsor_id      uuid                    not null
        references sponsors
);

create table if not exists project_contributor_labels
(
    id              uuid                                   not null
        primary key,
    slug            text                                   not null,
    project_id      uuid                                   not null
        references projects
            on delete cascade,
    name            text                                   not null,
    tech_updated_at timestamptz default now() not null,
    tech_created_at timestamptz default now() not null
);

create unique index if not exists project_contributor_labels_slug_index
    on project_contributor_labels (slug);

create index if not exists project_contributor_labels_project_id_index
    on project_contributor_labels (project_id);

create table if not exists contributor_project_contributor_labels
(
    github_user_id  bigint                                 not null,
    label_id        uuid                                   not null
        references project_contributor_labels,
    tech_updated_at timestamptz default now() not null,
    tech_created_at timestamptz default now() not null,
    primary key (github_user_id, label_id)
);

create unique index if not exists contributor_project_contributor_labels_pk_inv
    on contributor_project_contributor_labels (label_id, github_user_id);

create table if not exists p_user_project_recommendations
(
    pk              text                                   not null
        primary key,
    project_id      uuid,
    github_user_id  bigint,
    user_id         uuid,
    rank            bigint,
    hash            text,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null
);

create unique index if not exists public_p_user_project_recommendations_pk_hash_idx
    on p_user_project_recommendations (pk, hash);

create unique index if not exists p_user_project_recommendations_github_user_id_project_id_idx
    on p_user_project_recommendations (github_user_id asc, project_id desc);

create unique index if not exists p_user_project_recommendation_github_user_id_rank_project_i_idx
    on p_user_project_recommendations (github_user_id asc, rank asc, project_id desc);

create unique index if not exists p_user_project_recommendations_user_id_project_id_idx
    on p_user_project_recommendations (user_id asc, project_id desc);

create unique index if not exists p_user_project_recommendations_user_id_rank_project_id_idx
    on p_user_project_recommendations (user_id asc, rank asc, project_id desc);

create table if not exists archived_github_contributions
(
    contribution_uuid uuid                    not null
        primary key,
    tech_created_at   timestamptz default now() not null,
    tech_updated_at   timestamptz default now() not null
);



