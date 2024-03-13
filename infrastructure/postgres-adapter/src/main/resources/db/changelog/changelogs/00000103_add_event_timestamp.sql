alter table accounting.account_books_events
    add timestamp timestamp;

alter table accounting.account_books_events
    alter column timestamp set not null;

create index account_books_events_timestamp_idx
    on accounting.account_books_events (account_book_id, timestamp, id);