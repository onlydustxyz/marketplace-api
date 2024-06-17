create type application_origin as enum ('GITHUB', 'MARKETPLACE');
commit;

alter table applications
    add column origin application_origin not null default 'MARKETPLACE';
