create schema temp;

create materialized view temp.refunds as
select id,
       account_book_id,
       timestamp,
       (payload #>> '{event, from, id}')::uuid as sponsor_account_id,
       payload
from accounting.account_books_events abe
where payload #>> '{event, @type}' = 'Refund'
  and payload #> '{event, to, type}' is null;

select insert_account_books_event(
               r.id,
               r.account_book_id,
               jsonb_set(
                       jsonb_set(r.payload, '{event, to}', payload #> '{event, from}', false),
                       '{event, from}', jsonb_build_object('type', '"PROGRAM"', 'id', p.id), false),
               r.timestamp - interval '1 second')
from temp.refunds r
         join accounting.sponsor_accounts sa on sa.id = r.sponsor_account_id
         join sponsors s on sa.sponsor_id = s.id
         join programs p on p.name = s.name
order by r.id desc;

drop schema temp cascade;