alter table project_categories
    drop column status;

alter table project_categories
    rename column icon_url to icon_slug;

alter table project_categories
    alter column icon_slug set not null;

alter table project_categories
    add column tech_created_at timestamp not null default now(),
    add column tech_updated_at timestamp not null default now();

create trigger project_categories_set_tech_updated_at
    before update
    on project_categories
    for each row
execute procedure set_tech_updated_at();

