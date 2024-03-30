drop sequence accounting.account_books_events_id_seq cascade;

alter table accounting.account_books_events
    drop constraint account_books_events_pkey;

update accounting.account_books_events
set id = e.new_id
from (select abe.id, row_number() over (partition by account_book_id order by abe.timestamp) as new_id
      from accounting.account_books_events abe) e
where accounting.account_books_events.id = e.id;

alter table accounting.account_books_events
    add primary key (account_book_id, id);
