-- Needed to make migrations pass
create schema if not exists auth;
create table if not exists auth.users
(
    id uuid primary key
);
create table if not exists auth.user_providers
(
    id uuid primary key
);

update payment_requests
set currency = 'eth'
where id = '1c06c18c-1b76-4859-a093-f262b4f8e800'
   or id = '966cd55c-7de8-45c4-8bba-b388c38ca15d';

update payment_requests
set currency = 'strk'
where id = 'f0c1b882-76f2-47d0-9331-151ce1f99281'
   or id = 'b31a4ef1-b5f7-4560-bf5d-b47983069509';

update payment_requests
set currency = 'usd'
where id = '0341317f-b831-412a-9cec-a5a16a9d749c';

insert into currencies (id, name, code, decimals, type)
values ('3f6e1c98-8659-493a-b941-943a803bd91f', 'Bitcoin', 'BTC', 8, 'CRYPTO'),
       ('81b7e948-954f-4718-bad3-b70a0edd27e1', 'StarkNet Token', 'STRK', 18, 'CRYPTO');


