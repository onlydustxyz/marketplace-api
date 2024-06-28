CREATE TABLE banners_closed_by
(
    banner_id       uuid      not null references banners (id),
    user_id         uuid      not null references iam.users (id),
    tech_created_at timestamp not null default now(),
    tech_updated_at timestamp not null default now(),
    primary key (banner_id, user_id)
);

create trigger banners_closed_by_set_tech_updated_at
    before update
    on banners_closed_by
    for each row
execute function set_tech_updated_at();
