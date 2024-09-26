create index if not exists account_book_transactions_currency_sponsor on accounting.account_book_transactions (currency_id, sponsor_id);
create index if not exists account_book_transactions_currency_program on accounting.account_book_transactions (currency_id, program_id);
create index if not exists account_book_transactions_currency_project on accounting.account_book_transactions (currency_id, project_id);
create index if not exists account_book_transactions_currency_reward on accounting.account_book_transactions (currency_id, reward_id, amount);
create index if not exists account_book_transactions_currency_payment on accounting.account_book_transactions (currency_id, payment_id);

create index if not exists account_book_transactions_currency_sponsor_program_type_amount on accounting.account_book_transactions (currency_id, sponsor_id, program_id, type, amount);
create index if not exists account_book_transactions_currency_program_project_type_amount on accounting.account_book_transactions (currency_id, program_id, project_id, type, amount);
create index if not exists account_book_transactions_currency_project_reward_type_amount on accounting.account_book_transactions (currency_id, project_id, reward_id, type, amount);
create index if not exists account_book_transactions_currency_reward_type_amount on accounting.account_book_transactions (currency_id, reward_id, type, amount);
