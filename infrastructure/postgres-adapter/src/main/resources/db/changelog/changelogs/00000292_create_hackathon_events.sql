CREATE TABLE hackathon_events
(
    id              UUID      NOT NULL PRIMARY KEY,
    hackathon_id    UUID      NOT NULL REFERENCES hackathons (id),
    name            TEXT      NOT NULL,
    subtitle        TEXT      NOT NULL,
    icon_slug       TEXT      NOT NULL,
    start_at        TIMESTAMP NOT NULL,
    end_at          TIMESTAMP NOT NULL,
    links           JSONB     NOT NULL DEFAULT '[]',

    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER hackathon_events_set_tech_updated_at
    BEFORE UPDATE
    ON hackathon_events
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();