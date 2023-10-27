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

INSERT INTO "public"."budgets" ("id", "initial_amount", "remaining_amount", "currency")
VALUES ('6b8dd00f-9d0a-41c3-bf1d-08a23d0fd597', 200, 200, 'eth');

INSERT INTO "public"."projects_budgets" ("project_id", "budget_id")
VALUES ('f39b827f-df73-498c-8853-99bc3f562723', '6b8dd00f-9d0a-41c3-bf1d-08a23d0fd597');