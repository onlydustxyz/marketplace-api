CREATE TYPE committee_status AS ENUM (
    'DRAFT',
    'OPEN_TO_APPLICATIONS',
    'OPEN_TO_VOTES',
    'CLOSED'
    );

CREATE TABLE committees
(
    id                UUID PRIMARY KEY,
    status            committee_status NOT NULL,
    name              TEXT             NOT NULL,
    start_date        TIMESTAMP        NOT NULL,
    end_date          TIMESTAMP        NOT NULL,
    sponsor_id        UUID,
    tech_created_at   TIMESTAMP        NOT NULL DEFAULT now(),
    tech_updated_at   TIMESTAMP        NOT NULL DEFAULT now()
);

CREATE TRIGGER committees_set_tech_updated_at
    BEFORE UPDATE
    ON committees
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE TABLE committee_project_questions
(
    id              uuid      not null primary key,
    committee_id    uuid      not null,
    question        text      not null,
    required        boolean   not null,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    FOREIGN KEY (committee_id) references committees (id)
);

CREATE TRIGGER committee_project_questions_set_tech_updated_at
    BEFORE UPDATE
    ON committee_project_questions
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE TABLE committee_project_answers
(
    committee_id    uuid      not null,
    project_id      uuid      not null,
    question_id     uuid      not null,
    user_id         uuid      not null,
    answer          text,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (committee_id, project_id, question_id),
    FOREIGN KEY (committee_id) references committees (id),
    FOREIGN KEY (project_id) references projects (id),
    FOREIGN KEY (question_id) references committee_project_questions (id)
);

CREATE TRIGGER committee_project_answers_set_tech_updated_at
    BEFORE UPDATE
    ON committee_project_answers
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();