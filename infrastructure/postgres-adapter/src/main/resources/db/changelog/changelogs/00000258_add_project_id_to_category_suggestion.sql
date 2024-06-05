alter table project_category_suggestions
    add column project_id uuid not null references projects (id);