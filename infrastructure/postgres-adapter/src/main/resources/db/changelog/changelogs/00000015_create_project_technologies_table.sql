CREATE TABLE project_technologies
(
    project_id   UUID PRIMARY KEY,
    technologies JSONB     NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (project_id) REFERENCES project_details (project_id)
);