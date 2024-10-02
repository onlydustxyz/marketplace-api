CREATE TABLE contributor_project_contributor_labels
(
    github_user_id  BIGINT                   NOT NULL,
    label_id        UUID                     NOT NULL references project_contributor_labels (id),
    tech_updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    tech_created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    PRIMARY KEY (github_user_id, label_id)
);

CREATE UNIQUE INDEX contributor_project_contributor_labels_pk_inv
    ON contributor_project_contributor_labels (label_id, github_user_id);

create trigger contributor_project_contributor_labels_set_tech_updated_at
    before update
    on contributor_project_contributor_labels
    for each row
execute procedure public.set_tech_updated_at();

CREATE INDEX project_contributor_labels_project_id_index
    ON project_contributor_labels (project_id);