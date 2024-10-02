DROP VIEW accounting.all_transactions;

ALTER TABLE accounting.account_book_transactions
    ADD COLUMN deposit_status accounting.deposit_status;
