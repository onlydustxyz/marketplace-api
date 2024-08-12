create table accounting.account_book_transactions
(
    index              bigserial primary key,
    timestamp          timestamp not null,
    account_book_id    uuid      not null references accounting.account_books (id),
    sponsor_account_id uuid,
    project_id         uuid,
    reward_id          uuid,
    payment_id         uuid,
    amount             numeric   not null
);

create index account_book_transactions_account_book_id_index
    on accounting.account_book_transactions (account_book_id);

create index account_book_transactions_sponsor_account_id_index
    on accounting.account_book_transactions (sponsor_account_id);

create index account_book_transactions_project_id_index
    on accounting.account_book_transactions (project_id);

create index account_book_transactions_reward_id_index
    on accounting.account_book_transactions (reward_id);

create index account_book_transactions_payment_id_index
    on accounting.account_book_transactions (payment_id);
