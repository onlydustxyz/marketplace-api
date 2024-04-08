create table sponsors_users
(
    sponsor_id      UUID      NOT NULL REFERENCES sponsors (id),
    user_id         UUID      NOT NULL REFERENCES iam.users (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, sponsor_id)
);
