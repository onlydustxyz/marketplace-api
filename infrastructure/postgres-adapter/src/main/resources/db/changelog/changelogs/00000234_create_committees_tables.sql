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
    project_questions jsonb,
    tech_created_at   TIMESTAMP        NOT NULL DEFAULT now(),
    tech_updated_at   TIMESTAMP        NOT NULL DEFAULT now()
);;

CREATE TRIGGER committees_set_tech_updated_at
    BEFORE UPDATE
    ON committees
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();
