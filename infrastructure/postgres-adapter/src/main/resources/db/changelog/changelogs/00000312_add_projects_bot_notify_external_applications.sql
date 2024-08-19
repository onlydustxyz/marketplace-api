alter table projects
    add column bot_notify_external_applications boolean not null default true;

alter table projects
    alter column bot_notify_external_applications drop default;
