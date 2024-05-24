CREATE TABLE committee_juries
(
    user_id         uuid      NOT NULL,
    committee_id    uuid      NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, committee_id),
    FOREIGN KEY (user_id) REFERENCES iam.users (id),
    FOREIGN KEY (committee_id) REFERENCES committees (id)
);

CREATE TABLE committee_jury_criteria
(
    id              uuid      NOT NULL,
    committee_id    uuid      NOT NULL,
    criteria        TEXT      NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (id),
    FOREIGN KEY (committee_id) REFERENCES committees (id)
);

CREATE TRIGGER committee_jury_criteria_set_tech_updated_at
    BEFORE UPDATE
    ON committee_jury_criteria
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE TABLE committee_jury_votes
(
    committee_id    uuid      NOT NULL,
    criteria_id     uuid      NOT NULL,
    project_id      uuid      NOT NULL,
    score           INTEGER,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (committee_id, criteria_id, project_id),
    FOREIGN KEY (committee_id) REFERENCES committees (id),
    FOREIGN KEY (criteria_id) REFERENCES committee_jury_criteria (id),
    FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TRIGGER committee_jury_votes_set_tech_updated_at
    BEFORE UPDATE
    ON committee_jury_votes
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

ALTER TABLE committees
    ADD COLUMN vote_per_jury INTEGER;