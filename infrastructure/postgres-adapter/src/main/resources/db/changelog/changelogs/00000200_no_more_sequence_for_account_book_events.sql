drop sequence accounting.account_books_events_id_seq;

alter table accounting.account_books_events
    drop constraint account_books_events_pkey;

alter table accounting.account_books_events
    add primary key (account_book_id, id);

update accounting.account_books_events
set id = (select row_number() over (partition by account_book_id order by e.timestamp)
          from accounting.account_books_events e
          where e.account_book_id = accounting.account_books_events.account_book_id
            and e.id = accounting.account_books_events.id);
