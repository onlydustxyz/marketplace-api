create table committee_budget_allocations
(
    committee_id    uuid    not null references committees (id),
    project_id      uuid    not null references projects (id),
    currency_id     uuid    not null references currencies (id),
    amount          numeric not null,
    tech_created_at timestamp with time zone default now(),
    tech_updated_at timestamp with time zone default now(),
    primary key (committee_id, currency_id, project_id)
);

create trigger committee_budget_allocations_tech_updated_at
    before update
    on committee_budget_allocations
    for each row
execute function set_tech_updated_at();