CREATE TABLE committee_juries
(
    user_id         uuid      NOT NULL,
    committee_id    uuid      NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, committee_id),
    FOREIGN KEY (user_id) REFERENCES iam.users (id),
    FOREIGN KEY (committee_id) REFERENCES committees (id)
);

