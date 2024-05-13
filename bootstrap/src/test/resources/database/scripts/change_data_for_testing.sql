-- Needed to make migrations pass
create schema if not exists auth;
create table if not exists auth.users
(
    id uuid primary key
);
create table if not exists auth.user_providers
(
    id uuid primary key
);

REFRESH MATERIALIZED VIEW global_users_ranks;

insert into languages (id, name)
values ('ca600cac-0f45-44e9-a6e8-25e21b0c6887', 'Rust'),
       ('75ce6b37-8610-4600-8d2d-753b50aeda1e', 'Typescript'),
       ('f57d0866-89f3-4613-aaa2-32f4f4ecc972', 'Cairo'),
       ('d69b6d3e-f583-4c98-92d0-99a56f6f884a', 'Solidity');

insert into public.language_file_extensions (extension, language_id)
values ('rs', 'ca600cac-0f45-44e9-a6e8-25e21b0c6887'),
       ('tsx', '75ce6b37-8610-4600-8d2d-753b50aeda1e'),
       ('ts', '75ce6b37-8610-4600-8d2d-753b50aeda1e'),
       ('cairo', 'f57d0866-89f3-4613-aaa2-32f4f4ecc972'),
       ('sol', 'd69b6d3e-f583-4c98-92d0-99a56f6f884a');