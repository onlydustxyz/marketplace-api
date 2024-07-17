CREATE TABLE hackathon_sponsors
(
    hackathon_id    UUID      NOT NULL REFERENCES hackathons (id),
    sponsor_id      UUID      NOT NULL REFERENCES sponsors (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (hackathon_id, sponsor_id)
);

CREATE TRIGGER hackathon_sponsors_set_tech_updated_at
    BEFORE UPDATE
    ON hackathon_sponsors
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE TABLE hackathon_projects
(
    hackathon_id    UUID      NOT NULL REFERENCES hackathons (id),
    project_id      UUID      NOT NULL REFERENCES projects (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (hackathon_id, project_id)
);

CREATE TRIGGER hackathon_projects_set_tech_updated_at
    BEFORE UPDATE
    ON hackathon_projects
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

INSERT INTO hackathon_sponsors (hackathon_id, sponsor_id)
SELECT DISTINCT h.id, sponsor.id
FROM hackathons h
         CROSS JOIN unnest(h.sponsor_ids) as sponsor(id);

INSERT INTO hackathon_projects (hackathon_id, project_id)
SELECT DISTINCT h.id, project.id
FROM hackathons h
         JOIN LATERAL (SELECT jsonb_array_elements_text(jsonb_array_elements(h2.tracks) -> 'projectIds')::text::uuid as id
                       FROM hackathons h2
                       WHERE h2.id = h.id) as project ON TRUE;

ALTER TABLE hackathons
    ADD COLUMN github_labels   text[] NOT NULL DEFAULT '{}',
    ADD COLUMN community_links jsonb  NOT NULL DEFAULT '[]',
    DROP COLUMN sponsor_ids,
    DROP COLUMN tracks,
    ALTER COLUMN subtitle DROP NOT NULL;