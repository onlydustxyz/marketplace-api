CREATE TYPE hackathon_status AS ENUM (
    'DRAFT',
    'PUBLISHED'
    );

CREATE TABLE hackathons
(
    id              UUID PRIMARY KEY,
    slug            TEXT             NOT NULL,
    status          hackathon_status NOT NULL,
    title           TEXT             NOT NULL,
    subtitle        TEXT             NOT NULL,
    description     TEXT,
    location        TEXT,
    budget          TEXT,
    start_date      DATE             NOT NULL,
    end_date        DATE             NOT NULL,
    links           JSONB            NOT NULL,
    sponsor_ids     UUID[]           NOT NULL,
    tracks          JSONB            NOT NULL,
    tech_created_at TIMESTAMP        NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP        NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX hackathons_slug_uindex
    ON hackathons (slug);

CREATE TRIGGER hackathons_set_tech_updated_at
    BEFORE UPDATE
    ON hackathons
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();