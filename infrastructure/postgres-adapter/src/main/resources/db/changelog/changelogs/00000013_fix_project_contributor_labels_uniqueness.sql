drop index if exists project_contributor_labels_slug_index;
create unique index if not exists project_contributor_labels_slug_project_id_index
    on project_contributor_labels (slug, project_id);
create unique index if not exists project_contributor_labels_slug_project_id_index_inv
    on project_contributor_labels (project_id, slug);