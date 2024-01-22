CREATE TABLE hidden_contributors
(
    project_id                 UUID    NOT NULL,
    project_lead_id            UUID    NOT NULL,
    contributor_github_user_id INTEGER NOT NULL,
    PRIMARY KEY (project_id, project_lead_id, contributor_github_user_id)
);
