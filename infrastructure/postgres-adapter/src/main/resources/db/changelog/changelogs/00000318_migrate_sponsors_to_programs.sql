-- Migrate programs from sponsors
create table programs
(
    id              uuid primary key,
    name            text      not null,
    url             text,
    logo_url        text,
    tech_created_at timestamp not null default now(),
    tech_updated_at timestamp not null default now()
);

create trigger programs_set_tech_updated_at
    before update
    on programs
    for each row
execute function set_tech_updated_at();

insert into programs (id, name, logo_url, url)
select gen_random_uuid(), name, logo_url, url
from sponsors;

-- Migrate program leads from sponsors_users

create table program_leads
(
    program_id      uuid      not null references programs (id),
    user_id         uuid      not null references iam.users (id),
    tech_created_at timestamp not null default now(),
    tech_updated_at timestamp not null default now(),
    primary key (program_id, user_id)
);

create trigger program_leads_set_tech_updated_at
    before update
    on program_leads
    for each row
execute function set_tech_updated_at();

insert into program_leads (program_id, user_id)
select p.id, su.user_id
from sponsors_users su
         join sponsors s on su.sponsor_id = s.id
         join programs p on p.name = s.name;

-- Migrate sponsors_users to sponsor_leads
truncate table sponsors_users;

alter table sponsors_users
    rename to sponsor_leads;


-- Migrate account book events:

-- Mint on sponsor accounts -> transfer all to program
create schema temp;

create materialized view temp.mints as
select id,
       account_book_id,
       timestamp,
       (payload #>> '{event, account, id}')::uuid as sponsor_account_id,
       payload
from accounting.account_books_events abe
where payload #>> '{event, @type}' = 'Mint'
  and payload #>> '{event, account, type}' = 'SPONSOR_ACCOUNT';

select insert_account_books_event(
               m.id + 1,
               m.account_book_id,
               jsonb_build_object('event', jsonb_build_object(
                       '@type', 'Transfer',
                       'from', jsonb_build_object('id', m.sponsor_account_id, 'type', 'SPONSOR_ACCOUNT'),
                       'to', jsonb_build_object('id', p.id, 'type', 'PROGRAM'),
                       'amount', m.payload #> '{event, amount}')),
               m.timestamp + interval '1 second')
from temp.mints m
         join accounting.sponsor_accounts sa on sa.id = m.sponsor_account_id
         join sponsors s on sa.sponsor_id = s.id
         join programs p on p.name = s.name
order by m.id desc;

drop schema temp cascade;

-- Transfer to projects -> update origin to program
with transfers as (select id,
                          account_book_id,
                          (payload #>> '{event, from, id}')::uuid as sponsor_account_id,
                          payload
                   from accounting.account_books_events abe
                   where payload #>> '{event, @type}' = 'Transfer'
                     and payload #>> '{event, from, type}' = 'SPONSOR_ACCOUNT'
                     and payload #>> '{event, to, type}' = 'PROJECT')
update accounting.account_books_events
set payload = jsonb_set(
        jsonb_set(t.payload, '{event, from, id}', to_jsonb(p.id), false),
        '{event, from, type}', '"PROGRAM"', false)
from transfers t
         join accounting.sponsor_accounts sa on sa.id = t.sponsor_account_id
         join sponsors s on sa.sponsor_id = s.id
         join programs p on p.name = s.name
where account_books_events.account_book_id = t.account_book_id
  and account_books_events.id = t.id;

-- account book events: Refunds from projects
with refunds as (select id,
                        account_book_id,
                        (payload #>> '{event, to, id}')::uuid as sponsor_account_id,
                        payload
                 from accounting.account_books_events abe
                 where payload #>> '{event, @type}' = 'Refund'
                   and payload #>> '{event, to, type}' = 'SPONSOR_ACCOUNT')
update accounting.account_books_events
set payload = jsonb_set(
        jsonb_set(r.payload, '{event, to, id}', to_jsonb(p.id), false),
        '{event, to, type}', '"PROGRAM"', false)
from refunds r
         join accounting.sponsor_accounts sa on sa.id = r.sponsor_account_id
         join sponsors s on sa.sponsor_id = s.id
         join programs p on p.name = s.name
where account_books_events.account_book_id = r.account_book_id
  and account_books_events.id = r.id;

-- re-create account_book_transactions table
drop view accounting.all_sponsor_account_transactions;
drop view bi.program_stats;
drop view bi.program_stats_per_currency;
drop view bi.program_stats_per_currency_per_project;
drop view bi.project_stats_per_currency;

drop table accounting.account_book_transactions;

create table accounting.account_book_transactions
(
    id                 uuid primary key,
    timestamp          timestamp                   not null,
    account_book_id    uuid                        not null references accounting.account_books (id) deferrable initially deferred,
    type               accounting.transaction_type not null,
    sponsor_account_id uuid,
    program_id         uuid,
    project_id         uuid,
    reward_id          uuid,
    payment_id         uuid,
    amount             numeric                     not null
);

CREATE VIEW accounting.all_sponsor_account_transactions AS
SELECT id         AS id,
       account_id AS sponsor_account_id,
       timestamp  AS timestamp,
       type       AS type,
       amount     AS amount,
       NULL       AS program_id,
       network    AS network
FROM accounting.sponsor_account_transactions
UNION
SELECT gen_random_uuid()  AS id,
       sponsor_account_id AS sponsor_account_id,
       timestamp          AS timestamp,
       type               AS type,
       amount             AS amount,
       program_id         AS program_id,
       NULL               AS network
FROM accounting.account_book_transactions
WHERE sponsor_account_id IS NOT NULL
  AND project_id IS NULL
  AND reward_id IS NULL
  AND payment_id IS NULL;

create view bi.sponsor_stats_per_currency_per_program as
select sa.sponsor_id                                                                                        as sponsor_id,
       ab.currency_id                                                                                       as currency_id,
       abt.program_id                                                                                       as program_id,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.project_id is null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.project_id is null), 0)     as total_allocated,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.project_id is not null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.project_id is not null), 0) as total_granted
from accounting.account_book_transactions abt
         join accounting.account_books ab on abt.account_book_id = ab.id
         join accounting.sponsor_accounts sa on sa.id = abt.sponsor_account_id
where abt.sponsor_account_id is not null
  and abt.program_id is not null
  and abt.reward_id is null
group by sa.sponsor_id,
         ab.currency_id,
         abt.program_id;

create view bi.sponsor_stats_per_currency as
with virtual_stats as (select sa.sponsor_id                                                                                                  as sponsor_id,
                              sa.currency_id                                                                                                 as currency_id,
                              sum(abt.amount) filter (where abt.type = 'MINT' and abt.program_id is null)                                    as minted,
                              sum(abt.amount) filter (where abt.type = 'REFUND' and abt.program_id is null)                                  as refunded,
                              sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.program_id is not null and abt.project_id is null) as allocated,
                              sum(abt.amount) filter (where abt.type = 'REFUND' and abt.program_id is not null and abt.project_id is null)   as unallocated,
                              sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.reward_id is not null and abt.payment_id is null)  as rewarded,
                              sum(abt.amount) filter (where abt.type = 'REFUND' and abt.reward_id is not null and abt.payment_id is null)    as canceled,
                              sum(abt.amount) filter (where abt.type = 'BURN' and abt.reward_id is not null)                                 as paid
                       from accounting.account_book_transactions abt
                                join accounting.sponsor_accounts sa on sa.id = abt.sponsor_account_id
                       group by sa.sponsor_id,
                                sa.currency_id),
     physical_stats as (select sa.sponsor_id                                     as sponsor_id,
                               sa.currency_id                                    as currency_id,
                               sum(sat.amount) filter ( where type = 'DEPOSIT')  as deposited,
                               sum(sat.amount) filter ( where type = 'WITHDRAW') as withdrawn,
                               sum(sat.amount) filter ( where type = 'SPEND')    as spent
                        from accounting.sponsor_account_transactions sat
                                 join accounting.sponsor_accounts sa on sa.id = sat.account_id
                        group by sa.sponsor_id,
                                 sa.currency_id)
select coalesce(vs.sponsor_id, ps.sponsor_id)                  as sponsor_id,
       coalesce(vs.currency_id, ps.currency_id)                as currency_id,
       coalesce(vs.minted, 0) - coalesce(vs.refunded, 0)       as initial_allowance,
       coalesce(vs.allocated, 0) - coalesce(vs.unallocated, 0) as total_allocated,
       coalesce(vs.rewarded, 0) - coalesce(vs.canceled, 0)     as total_rewarded,
       coalesce(vs.paid, 0)                                    as total_paid,
       coalesce(ps.deposited, 0) - coalesce(ps.withdrawn, 0)   as initial_balance,
       coalesce(ps.spent, 0)                                   as total_spent
from virtual_stats vs
         full join physical_stats ps on vs.sponsor_id = ps.sponsor_id and vs.currency_id = ps.currency_id;

create view bi.program_stats_per_currency_per_project as
select abt.program_id                                                                                      as program_id,
       ab.currency_id                                                                                      as currency_id,
       abt.project_id                                                                                      as project_id,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.reward_id is null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.reward_id is null), 0)     as total_granted,
       coalesce(sum(abt.amount) filter (where abt.type = 'TRANSFER' and abt.reward_id is not null), 0)
           - coalesce(sum(abt.amount) filter (where abt.type = 'REFUND' and abt.reward_id is not null), 0) as total_rewarded
from accounting.account_book_transactions abt
         join accounting.account_books ab on abt.account_book_id = ab.id
where abt.program_id is not null
  and abt.project_id is not null
  and abt.payment_id is null
group by abt.program_id,
         ab.currency_id,
         abt.project_id;


create view bi.program_stats_per_currency as
select abt.program_id                                                                                                           as program_id,
       ab.currency_id                                                                                                           as currency_id,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.project_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and abt.project_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is null ), 0)
           + coalesce(sum(amount) filter ( where type = 'REFUND' and abt.project_id is not null and abt.reward_id is null ), 0) as total_available,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and abt.project_id is not null and abt.reward_id is null ), 0) as total_granted,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and abt.reward_id is not null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and abt.reward_id is not null ), 0)                            as total_rewarded

from accounting.account_book_transactions abt
         join accounting.account_books ab on abt.account_book_id = ab.id
where abt.program_id is not null
  and abt.payment_id is null
group by abt.program_id,
         ab.currency_id;

create view bi.program_stats as
select p.id                                      as program_id,
       coalesce(count(distinct s.project_id), 0) as granted_project_count
from programs p
         left join bi.program_stats_per_currency_per_project s on p.id = s.program_id and s.total_granted > 0
group by p.id;

create view bi.project_stats_per_currency as
select abt.project_id                                                                            as project_id,
       ab.currency_id                                                                            as currency_id,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and reward_id is null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and reward_id is null ), 0)     as total_granted,

       coalesce(sum(amount) filter ( where type = 'TRANSFER' and reward_id is not null ), 0)
           - coalesce(sum(amount) filter ( where type = 'REFUND' and reward_id is not null ), 0) as total_rewarded

from accounting.account_book_transactions abt
         join accounting.account_books ab on abt.account_book_id = ab.id
where abt.project_id is not null
  and payment_id is null
group by abt.project_id,
         ab.currency_id;

-- remove projects_sponsors table

-- Seen with Paco, will be re-created later for metabase
drop view bi.monthly_contributions_stats_per_sponsor;
drop view bi.monthly_rewards_creation_stats_per_currency_sponsor;
drop view bi.monthly_rewards_creation_stats_per_sponsor;
drop view bi.weekly_contributions_stats_per_sponsor;
drop view bi.weekly_rewards_creation_stats_per_currency_sponsor;
drop view bi.weekly_rewards_creation_stats_per_sponsor;

drop table projects_sponsors;

create view programs_projects as
with allocations as (select abt.account_book_id,
                            abt.program_id,
                            abt.project_id,
                            sum(abt.amount) filter ( where abt.type = 'TRANSFER' )
                                - sum(abt.amount) filter ( where abt.type = 'REFUND' ) as amount
                     from accounting.account_book_transactions abt
                     where abt.project_id is not null
                       and abt.reward_id is null
                     group by abt.account_book_id, abt.program_id, abt.project_id)
select distinct program_id, project_id
from allocations
where amount > 0;

create view sponsors_programs as
select distinct sa.sponsor_id  as sponsor_id,
                abt.program_id as program_id
from accounting.account_book_transactions abt
         join accounting.sponsor_accounts sa on sa.id = abt.sponsor_account_id
where program_id is not null
group by sa.sponsor_id, abt.program_id;
