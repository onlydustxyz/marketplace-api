CREATE TABLE projects_project_categories
(
    project_id          UUID      NOT NULL REFERENCES projects (id),
    project_category_id UUID      NOT NULL REFERENCES project_categories (id),
    tech_created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    tech_updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (project_id, project_category_id)
);

CREATE TRIGGER projects_project_categories_set_tech_updated_at
    BEFORE UPDATE
    ON projects_project_categories
    FOR EACH ROW
EXECUTE FUNCTION set_tech_updated_at();