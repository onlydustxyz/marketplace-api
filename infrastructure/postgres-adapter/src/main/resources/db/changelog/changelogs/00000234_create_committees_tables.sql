CREATE TYPE committee_status AS ENUM (
    'DRAFT',
    'OPEN_FOR_APPLICATIONS',
    'OPEN_FOR_VOTES',
    'CLOSED'
    );

CREATE TABLE committees
(
    id              UUID PRIMARY KEY,
    status          committee_status NOT NULL,
    name           TEXT             NOT NULL,
    start_date      DATE             NOT NULL,
    end_date        DATE             NOT NULL,
    tech_created_at TIMESTAMP        NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP        NOT NULL DEFAULT now()
);;

CREATE TRIGGER committees_set_tech_updated_at
    BEFORE UPDATE
    ON committees
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();
