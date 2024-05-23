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
