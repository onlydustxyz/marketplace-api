alter table project_categories
    add column slug text not null;

alter table project_categories
    add constraint project_categories_slug_key unique (slug);
