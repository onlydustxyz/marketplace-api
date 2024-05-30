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

REFRESH MATERIALIZED VIEW global_users_ranks;

insert into languages (id, name)
values ('ca600cac-0f45-44e9-a6e8-25e21b0c6887', 'Rust'),
       ('75ce6b37-8610-4600-8d2d-753b50aeda1e', 'Typescript'),
       ('f57d0866-89f3-4613-aaa2-32f4f4ecc972', 'Cairo'),
       ('d69b6d3e-f583-4c98-92d0-99a56f6f884a', 'Solidity');

insert into public.language_file_extensions (extension, language_id)
values ('rs', 'ca600cac-0f45-44e9-a6e8-25e21b0c6887'),
       ('tsx', '75ce6b37-8610-4600-8d2d-753b50aeda1e'),
       ('ts', '75ce6b37-8610-4600-8d2d-753b50aeda1e'),
       ('cairo', 'f57d0866-89f3-4613-aaa2-32f4f4ecc972'),
       ('sol', 'd69b6d3e-f583-4c98-92d0-99a56f6f884a');

insert into ecosystem_banners (id, font_color, image_url)
values ('12ae34bb-dfbc-48ed-bef0-08c5c087593d', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/zama-md.png'),
       ('750f96b3-2abe-4dca-839a-4ef9820ff6bb', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/zama-xl.png'),
       ('47477e2f-29f5-43e1-ab75-cbfca6d583d1', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/starknet-md.png'),
       ('0c3f1b51-4dbe-4715-a63b-eadd311f0ef1', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/starknet-xl.png'),
       ('cc90e69c-490e-4a3f-a30f-5a98ab430ab3', 'LIGHT', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/aptos-md.png'),
       ('d0678613-31fd-43d4-aa6e-298d1528a6c2', 'LIGHT', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/aptos-xl.png'),
       ('1f2c1de8-8952-4974-869b-0b58e6972a08', 'LIGHT', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-md.png'),
       ('8a3c5cfc-daac-4742-841c-617976ae01a1', 'LIGHT', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/ethereum-xl.png'),
       ('759700b4-7ea7-4459-a6ff-d97ca6040828', 'LIGHT', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/lava-md.png'),
       ('0fb7780d-9970-40ef-9574-73a7a53e7b2d', 'LIGHT', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/lava-xl.png'),
       ('18518285-4b01-4f19-82b6-09578339bf4f', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/aztec-md.png'),
       ('ffb1c659-eb93-4b22-904e-114bf5cae214', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/aztec-xl.png'),
       ('472db10d-b910-4f9e-9741-8be3c894563d', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/optimism-md.png'),
       ('bf5b4fb1-8b2c-4772-828d-3ea2d3a2e10d', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/optimism-xl.png'),
       ('d0853c84-efe8-47a9-b268-7b32cb9daa26', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/avail-md.png'),
       ('182d37e3-3d16-4760-88ef-0f8f9cfb7ed5', 'DARK', 'https://s3.amazonaws.com/onlydust/ecosystem_banners/avail-xl.png')
;

update ecosystems
set description = name || ' ecosystem';

update ecosystems
set md_banner_id = b.id
from ecosystem_banners b
where b.image_url = 'https://s3.amazonaws.com/onlydust/ecosystem_banners/' || slug || '-md.png';

update ecosystems
set xl_banner_id = b.id
from ecosystem_banners b
where b.image_url = 'https://s3.amazonaws.com/onlydust/ecosystem_banners/' || slug || '-xl.png';

update ecosystems
set featured_rank = 1
where slug = 'ethereum';

update ecosystems
set featured_rank = 2
where slug = 'starknet';

update ecosystems
set featured_rank = 3
where slug = 'aptos';

