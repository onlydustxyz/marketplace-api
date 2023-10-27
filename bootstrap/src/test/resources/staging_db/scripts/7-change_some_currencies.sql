update payment_requests
set currency = 'eth'
where id = '1c06c18c-1b76-4859-a093-f262b4f8e800'
   or id = '966cd55c-7de8-45c4-8bba-b388c38ca15d';

update payment_requests
set currency = 'stark'
where id = 'f0c1b882-76f2-47d0-9331-151ce1f99281'
   or id = 'b31a4ef1-b5f7-4560-bf5d-b47983069509';

insert into crypto_usd_quotes (currency, price, updated_at)
VALUES ('eth', 1781.98, now());