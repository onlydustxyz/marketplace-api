BEGIN;
create type banner_font_color as enum ('DARK', 'LIGHT');
END;

create table ecosystem_banners
(
    id              UUID PRIMARY KEY,
    font_color      banner_font_color NOT NULL,
    image_url       text              NOT NULL,
    tech_created_at timestamp         NOT NULL DEFAULT now(),
    tech_updated_at timestamp         NOT NULL DEFAULT now()
);

CREATE TRIGGER ecosystem_banners_set_tech_updated_at
    BEFORE UPDATE
    ON ecosystem_banners
    FOR EACH ROW
EXECUTE FUNCTION set_tech_updated_at();

alter table ecosystems
    add slug            text,
    add description     text,
    add md_banner_id    UUID references ecosystem_banners (id),
    add xl_banner_id    UUID references ecosystem_banners (id),
    add tech_created_at timestamp not null default now(),
    add tech_updated_at timestamp not null default now();

CREATE TRIGGER ecosystems_set_tech_updated_at
    BEFORE UPDATE
    ON ecosystems
    FOR EACH ROW
EXECUTE FUNCTION set_tech_updated_at();

update ecosystems
set slug = lower(replace(regexp_replace(name, '[^a-zA-Z0-9_\-\ ]+', '', 'g'), ' ', '-'));

alter table ecosystems
    alter column slug set not null;
