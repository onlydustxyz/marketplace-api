create table accounting.account_book_transactions
(
    index              bigserial primary key,
    timestamp          timestamp                   not null,
    account_book_id    uuid                        not null references accounting.account_books (id),
    type               accounting.transaction_type not null,
    sponsor_account_id uuid,
    project_id         uuid,
    reward_id          uuid,
    payment_id         uuid,
    amount             numeric                     not null
);
