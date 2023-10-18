create table global_settings
(
    id                                       serial primary key,
    terms_and_conditions_latest_version_date timestamp not null
);

insert into global_settings (terms_and_conditions_latest_version_date)
values ('2023-06-01 00:00:00'::timestamp);