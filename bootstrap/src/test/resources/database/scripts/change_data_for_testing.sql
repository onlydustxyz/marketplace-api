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

insert into projects_ecosystems (project_id, ecosystem_id)
values ('7d04163c-4187-4313-8066-61504d34fc56',(select s.id from ecosystems s where s.name = 'Zama'));
insert into projects_ecosystems (project_id, ecosystem_id)
values ('7d04163c-4187-4313-8066-61504d34fc56',(select s.id from ecosystems s where s.name = 'Ethereum'));
insert into projects_ecosystems (project_id, ecosystem_id)
values ('7d04163c-4187-4313-8066-61504d34fc56',(select s.id from ecosystems s where s.name = 'Aptos'));
insert into projects_ecosystems (project_id, ecosystem_id)
values ('27ca7e18-9e71-468f-8825-c64fe6b79d66',(select s.id from ecosystems s where s.name = 'Starknet'));
insert into projects_ecosystems (project_id, ecosystem_id)
values ('27ca7e18-9e71-468f-8825-c64fe6b79d66',(select s.id from ecosystems s where s.name = 'Optimism'));
insert into projects_ecosystems (project_id, ecosystem_id)
values ('27ca7e18-9e71-468f-8825-c64fe6b79d66',(select s.id from ecosystems s where s.name = 'Lava'));
insert into projects_ecosystems (project_id, ecosystem_id)
values ('1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e',(select s.id from ecosystems s where s.name = 'Starknet'));
insert into projects_ecosystems (project_id, ecosystem_id)
values ('594ca5ca-48f7-49a8-9c26-84b949d4fdd9',(select s.id from ecosystems s where s.name = 'Aztec'));
insert into projects_ecosystems (project_id, ecosystem_id)
values ('90fb751a-1137-4815-b3c4-54927a5db059',(select s.id from ecosystems s where s.name = 'Avail'));







