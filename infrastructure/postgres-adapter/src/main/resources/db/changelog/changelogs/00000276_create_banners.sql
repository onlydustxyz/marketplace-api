CREATE TABLE banners
(
    id               uuid PRIMARY KEY,
    text             text      NOT NULL,
    button_text      text,
    button_icon_slug text,
    button_link_url  text,
    visible          boolean   not null,
    updated_at       timestamp not null,
    tech_created_at  timestamp not null default now(),
    tech_updated_at  timestamp not null default now()
);

create trigger banners_set_tech_updated_at
    before update
    on banners
    for each row
execute function set_tech_updated_at();

create index if not exists banners_visible_index
    on banners (visible);
