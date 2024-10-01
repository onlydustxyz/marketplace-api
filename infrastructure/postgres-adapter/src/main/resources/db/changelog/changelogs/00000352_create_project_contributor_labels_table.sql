CREATE TABLE project_contributor_labels
(
    id              UUID PRIMARY KEY,
    slug            text                     NOT NULL,
    project_id      UUID                     NOT NULL,
    name            text                     NOT NULL,
    tech_updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    tech_created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX project_contributor_labels_slug_index
    ON project_contributor_labels (slug);

create trigger project_contributor_labels_set_tech_updated_at
    before update
    on project_contributor_labels
    for each row
execute procedure public.set_tech_updated_at();