CREATE TYPE project_tag as enum ('HOT_COMMUNITY',
    'NEWBIES_WELCOME',
    'LIKELY_TO_REWARD',
    'WORK_IN_PROGRESS',
    'FAST_AND_FURIOUS',
    'BIG_WHALE',
    'UPDATED_ROADMAP');

CREATE TABLE projects_tags
(
    project_id uuid        not null,
    tag        project_tag not null,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW()
);
