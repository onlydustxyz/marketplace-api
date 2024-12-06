CREATE SCHEMA IF NOT EXISTS reco;

CREATE TABLE reco.matching_questions
(
    id                 UUID PRIMARY KEY,
    matching_system_id TEXT                      NOT NULL,
    index              INT                       NOT NULL,
    body               TEXT                      NOT NULL,
    description        TEXT,
    multiple_choice    BOOLEAN                   NOT NULL,
    tech_created_at    timestamptz default now() not null,
    tech_updated_at    timestamptz default now() not null
);

create trigger reco_matching_questions_set_tech_updated_at
    before update
    on reco.matching_questions
    for each row
execute procedure public.set_tech_updated_at();

create unique index idx_matching_questions_matching_system_id_index on reco.matching_questions (matching_system_id, index);

CREATE TABLE reco.matching_answers
(
    id              UUID PRIMARY KEY,
    question_id     UUID                      NOT NULL REFERENCES reco.matching_questions (id),
    index           INT                       NOT NULL,
    body            TEXT                      NOT NULL,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null
);

create trigger reco_matching_answers_set_tech_updated_at
    before update
    on reco.matching_answers
    for each row
execute procedure public.set_tech_updated_at();

create unique index idx_matching_answers_question_id_index on reco.matching_answers (question_id, index);

CREATE TABLE reco.user_answers
(
    user_id         UUID                      NOT NULL REFERENCES iam.users (id),
    question_id     UUID                      NOT NULL REFERENCES reco.matching_questions (id),
    answer_id       UUID                      NOT NULL REFERENCES reco.matching_answers (id),
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null,
    PRIMARY KEY (user_id, question_id, answer_id)
);

create trigger reco_user_answers_set_tech_updated_at
    before update
    on reco.user_answers
    for each row
execute procedure public.set_tech_updated_at();
