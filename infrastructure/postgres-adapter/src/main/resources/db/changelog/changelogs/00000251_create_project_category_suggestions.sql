create table project_category_suggestions
(
    id              UUID PRIMARY KEY,
    name            TEXT      NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TRIGGER project_category_suggestions_set_tech_updated_at
    BEFORE UPDATE
    ON project_category_suggestions
    FOR EACH ROW
EXECUTE FUNCTION set_tech_updated_at();

insert into project_category_suggestions (id, name)
select id, name
from project_categories
where status = 'SUGGESTED';

delete
from project_categories
where status = 'SUGGESTED';
