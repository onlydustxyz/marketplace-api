create schema if not exists rfd;

create table if not exists rfd.iso_currencies
(
    alpha_code      text                    not null
        primary key,
    name            text                    not null
        unique,
    numeric_code    integer                 not null
        unique,
    minor_unit      integer   default 0     not null,
    tech_created_at timestamptz default now() not null,
    tech_updated_at timestamptz default now() not null
);

create unique index if not exists idx_iso_currencies_numeric_code
    on rfd.iso_currencies (numeric_code);

create trigger update_rfd_iso_currencies_tech_updated_at
    before update
    on rfd.iso_currencies
    for each row
execute procedure public.set_tech_updated_at();
