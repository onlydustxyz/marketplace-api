create table accounting.payout_preferences
(
    billing_profile_id uuid                    not null,
    project_id         uuid                    not null,
    user_id            uuid                    not null,
    tech_created_at    timestamp default now() not null,
    tech_updated_at    timestamp default now() not null,
    primary key (project_id, user_id)
);