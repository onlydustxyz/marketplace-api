ALTER TABLE projects
    ADD COLUMN slug TEXT;

UPDATE projects
SET slug = key;

ALTER TABLE projects
    ALTER COLUMN slug SET NOT NULL;

CREATE UNIQUE INDEX projects_slug
    ON public.projects (slug);

ALTER TABLE projects
    DROP COLUMN key;
