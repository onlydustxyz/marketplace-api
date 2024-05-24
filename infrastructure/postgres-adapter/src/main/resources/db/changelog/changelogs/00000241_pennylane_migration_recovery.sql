-- remove duplicated rewards receipts due to pennylane migration
create table deduplicated_receipts as
select distinct receipts.id
from accounting.rewards_receipts rr
         join lateral ( select re.id
                        from accounting.rewards_receipts rr2
                                 join accounting.receipts re on rr2.receipt_id = re.id
                        where rr2.reward_id = rr.reward_id
                        order by re.created_at
                        limit 1
    ) receipts on true;

delete
from accounting.rewards_receipts
where receipt_id not in (select id from deduplicated_receipts);

delete
from accounting.receipts
where id not in (select receipt_id from accounting.rewards_receipts);

drop table deduplicated_receipts;

-- create utils function

CREATE OR REPLACE FUNCTION insert_account_books_event(_id bigint, _account_book_id uuid, _payload jsonb, _timestamp timestamp) RETURNS VOID AS
$$
BEGIN
    alter table accounting.account_books_events
        drop constraint account_books_events_pkey;
    update accounting.account_books_events set id = id + 1 where account_book_id = _account_book_id and id >= _id;
    alter table accounting.account_books_events
        add primary key (account_book_id, id);
    insert into accounting.account_books_events (id, account_book_id, payload, timestamp) values (_id, _account_book_id, _payload, _timestamp);
END
$$
    LANGUAGE 'plpgsql';

-- Usage example
-- SELECT insert_account_books_event(12, 'cfd68b31-6168-436a-91fa-83ecde4e0dd2', '{"foo": "bar"}', '2020-01-01 00:00:00');


CREATE OR REPLACE FUNCTION delete_account_books_event(_id bigint, _account_book_id uuid) RETURNS VOID AS
$$
BEGIN
    delete from accounting.account_books_events where account_book_id = _account_book_id and id = _id;

    alter table accounting.account_books_events
        drop constraint account_books_events_pkey;
    update accounting.account_books_events set id = id - 1 where account_book_id = _account_book_id and id > _id;
    alter table accounting.account_books_events
        add primary key (account_book_id, id);
END
$$
    LANGUAGE 'plpgsql';

-- Usage example
-- SELECT delete_account_books_event(12);


