ALTER TABLE accounting.billing_profile_verification_outbox_events
    ADD COLUMN group_key TEXT;

CREATE TABLE accounting.children_kyc
(
    applicant_id        text primary key,
    parent_applicant_id text                           not null,
    verification_status accounting.verification_status not null,
    tech_created_at     TIMESTAMP                      NOT NULL DEFAULT now(),
    tech_updated_at     TIMESTAMP                      NOT NULL DEFAULT now()
);

-- Company
insert into accounting.billing_profiles_users (billing_profile_id, user_id, role)
select gen_random_uuid(), cbp.user_id, cast('ADMIN' as accounting.billing_profile_role)
from company_billing_profiles cbp;

insert into accounting.billing_profiles (id, name, type, verification_status)
select bpu.billing_profile_id,
       coalesce(cpb.name, 'Company'),
       cast('SELF_EMPLOYED' as accounting.billing_profile_type),
       case
           when cpb.verification_status = 'VERIFIED' then cast('VERIFIED' as accounting.verification_status)
           when cpb.verification_status = 'REJECTED' then cast('REJECTED' as accounting.verification_status)
           when cpb.verification_status = 'CLOSED' then cast('CLOSED' as accounting.verification_status)
           when cpb.verification_status = 'NOT_STARTED' then cast('NOT_STARTED' as accounting.verification_status)
           when cpb.verification_status = 'UNDER_REVIEW' then cast('UNDER_REVIEW' as accounting.verification_status)
           when cpb.verification_status = 'STARTED' then cast('STARTED' as accounting.verification_status)
           end
from accounting.billing_profiles_users bpu
         join public.company_billing_profiles cpb on cpb.user_id = bpu.user_id
where bpu.billing_profile_id not in (select id from accounting.billing_profiles);

insert into accounting.kyb (id, owner_id, billing_profile_id, name, registration_number,
                            registration_date, address, country, us_entity, subject_to_eu_vat, eu_vat_number,
                            review_message, applicant_id, verification_status)
select cpb.id,
       cpb.user_id,
       bpu.billing_profile_id,
       cpb.name,
       cpb.registration_number,
       cpb.registration_date,
       cpb.address,
       cpb.country,
       cpb.us_entity,
       cpb.subject_to_eu_vat,
       cpb.eu_vat_number,
       cpb.review_message,
       cpb.applicant_id,
       case
           when cpb.verification_status = 'VERIFIED' then cast('VERIFIED' as accounting.verification_status)
           when cpb.verification_status = 'REJECTED' then cast('REJECTED' as accounting.verification_status)
           when cpb.verification_status = 'CLOSED' then cast('CLOSED' as accounting.verification_status)
           when cpb.verification_status = 'NOT_STARTED' then cast('NOT_STARTED' as accounting.verification_status)
           when cpb.verification_status = 'UNDER_REVIEW' then cast('UNDER_REVIEW' as accounting.verification_status)
           when cpb.verification_status = 'STARTED' then cast('STARTED' as accounting.verification_status)
           end
from accounting.billing_profiles_users bpu
         join public.company_billing_profiles cpb on cpb.user_id = bpu.user_id
         join accounting.billing_profiles bp on bp.id = bpu.billing_profile_id and bp.type = 'SELF_EMPLOYED';

-- Individual
insert into accounting.billing_profiles_users (billing_profile_id, user_id, role)
select gen_random_uuid(), ibp.user_id, cast('ADMIN' as accounting.billing_profile_role)
from individual_billing_profiles ibp;

insert into accounting.billing_profiles (id, name, type, verification_status)
select bpu.billing_profile_id,
       coalesce(ibp.first_name || ' ' || ibp.last_name, 'Individual'),
       cast('INDIVIDUAL' as accounting.billing_profile_type),
       case
           when ibp.verification_status = 'VERIFIED' then cast('VERIFIED' as accounting.verification_status)
           when ibp.verification_status = 'REJECTED' then cast('REJECTED' as accounting.verification_status)
           when ibp.verification_status = 'CLOSED' then cast('CLOSED' as accounting.verification_status)
           when ibp.verification_status = 'NOT_STARTED' then cast('NOT_STARTED' as accounting.verification_status)
           when ibp.verification_status = 'UNDER_REVIEW' then cast('UNDER_REVIEW' as accounting.verification_status)
           when ibp.verification_status = 'STARTED' then cast('STARTED' as accounting.verification_status)
           end
from accounting.billing_profiles_users bpu
         join public.individual_billing_profiles ibp on ibp.user_id = bpu.user_id
where bpu.billing_profile_id not in (select id from accounting.billing_profiles);

insert into accounting.kyc (id, owner_id, billing_profile_id, first_name, last_name, address,
                            country, birthdate, valid_until, id_document_number, id_document_country_code, us_citizen,
                            review_message, applicant_id, id_document_type, verification_status)
select ibp.id,
       ibp.user_id,
       bpu.billing_profile_id,
       ibp.first_name,
       ibp.last_name,
       ibp.address,
       ibp.country,
       ibp.birthdate,
       ibp.valid_until,
       ibp.id_document_number,
       ibp.id_document_country_code,
       ibp.us_citizen,
       ibp.review_message,
       ibp.applicant_id,
       case
           when ibp.id_document_type = 'PASSPORT' then cast('PASSPORT' as accounting.id_document_type)
           when ibp.id_document_type = 'ID_CARD' then cast('ID_CARD' as accounting.id_document_type)
           when ibp.id_document_type = 'RESIDENCE_PERMIT' then cast('RESIDENCE_PERMIT' as accounting.id_document_type)
           when ibp.id_document_type = 'DRIVER_LICENSE' then cast('DRIVER_LICENSE' as accounting.id_document_type)
           end,
       case
           when ibp.verification_status = 'VERIFIED' then cast('VERIFIED' as accounting.verification_status)
           when ibp.verification_status = 'REJECTED' then cast('REJECTED' as accounting.verification_status)
           when ibp.verification_status = 'CLOSED' then cast('CLOSED' as accounting.verification_status)
           when ibp.verification_status = 'NOT_STARTED' then cast('NOT_STARTED' as accounting.verification_status)
           when ibp.verification_status = 'UNDER_REVIEW' then cast('UNDER_REVIEW' as accounting.verification_status)
           when ibp.verification_status = 'STARTED' then cast('STARTED' as accounting.verification_status)
           end
from accounting.billing_profiles_users bpu
         join public.individual_billing_profiles ibp on ibp.user_id = bpu.user_id
         join accounting.billing_profiles bp on bp.id = bpu.billing_profile_id and bp.type = 'INDIVIDUAL';

insert into accounting.children_kyc (applicant_id, parent_applicant_id, verification_status)
select ck.applicant_id,
       ck.parent_applicant_id,
       case
           when ck.verification_status = 'VERIFIED' then cast('VERIFIED' as accounting.verification_status)
           when ck.verification_status = 'REJECTED' then cast('REJECTED' as accounting.verification_status)
           when ck.verification_status = 'CLOSED' then cast('CLOSED' as accounting.verification_status)
           when ck.verification_status = 'NOT_STARTED' then cast('NOT_STARTED' as accounting.verification_status)
           when ck.verification_status = 'UNDER_REVIEW' then cast('UNDER_REVIEW' as accounting.verification_status)
           when ck.verification_status = 'STARTED' then cast('STARTED' as accounting.verification_status)
           end
from public.children_kyc ck;

insert into accounting.wallets (billing_profile_id, address, network, type)
select bpu.billing_profile_id,
       w.address,
       case
           when w.network = 'ethereum' then cast('ethereum' as accounting.network)
           when w.network = 'aptos' then cast('aptos' as accounting.network)
           when w.network = 'starknet' then cast('starknet' as accounting.network)
           when w.network = 'optimism' then cast('optimism' as accounting.network)
           when w.network = 'sepa' then cast('sepa' as accounting.network)
           when w.network = 'swift' then cast('swift' as accounting.network)
           end,
       case
           when w.type = 'address' then cast('address' as accounting.wallet_type)
           when w.type = 'name' then cast('name' as accounting.wallet_type)
           end
from accounting.billing_profiles_users bpu
         join public.wallets w on w.user_id = bpu.user_id;


insert into accounting.bank_accounts (billing_profile_id, bic, number)
select bpu.billing_profile_id,
       ba.bic,
       ba.iban
from accounting.billing_profiles_users bpu
         join public.bank_accounts ba on ba.user_id = bpu.user_id;

drop table public.company_billing_profiles;
drop table public.individual_billing_profiles;
drop table public.children_kyc;
drop table public.wallets;
drop table public.bank_accounts;



