CREATE TYPE project_tag as enum ('BEGINNERS_WELCOME', 'STRONG_EXPERTISE' , 'LIKELY_TO_SEND_REWARDS','FAST_PACED');

CREATE TABLE projects_tags
(
    project_id uuid        not null,
    tag        project_tag not null,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW()
);
