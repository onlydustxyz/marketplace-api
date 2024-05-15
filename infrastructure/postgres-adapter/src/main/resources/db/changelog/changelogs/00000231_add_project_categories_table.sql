CREATE TYPE project_category_status AS ENUM ('SUGGESTED', 'APPROVED');

CREATE TABLE project_categories
(
    id       uuid primary key,
    name     text                    not null,
    status   project_category_status not null,
    icon_url text
);