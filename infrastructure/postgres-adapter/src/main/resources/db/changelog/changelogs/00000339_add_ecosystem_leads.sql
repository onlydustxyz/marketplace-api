create table ecosystem_leads
(
    ecosystem_id    uuid                     not null references ecosystems (id),
    user_id         uuid                     not null references iam.users (id),
    tech_created_at timestamp with time zone not null default now(),
    tech_updated_at timestamp with time zone not null default now(),
    primary key (ecosystem_id, user_id)
);

create trigger ecosystem_leads_set_tech_updated_at
    before update
    on ecosystem_leads
    for each row
execute function set_tech_updated_at();
