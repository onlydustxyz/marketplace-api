CREATE TYPE verification_status AS ENUM ('NOT_STARTED','STARTED','UNDER_REVIEW','VERIFIED','REJECTED','INVALIDATED','CLOSED');
CREATE TYPE id_document_type AS ENUM ('PASSPORT','ID_CARD','RESIDENCE_PERMIT','DRIVER_LICENSE');
CREATE TYPE billing_profile_type AS ENUM ('INDIVIDUAL','COMPANY');

CREATE TABLE individual_billing_profiles
(
    id                       uuid                NOT NULL,
    user_id                  uuid                NOT NULL,
    verification_status      verification_status NOT NULL,
    first_name               TEXT,
    last_name                TEXT,
    address                  TEXT,
    country                  TEXT,
    birthdate                TIMESTAMP,
    valid_until              TIMESTAMP,
    id_document_number       TEXT,
    id_document_type         id_document_type,
    id_document_country_code TEXT,
    us_citizen               BOOLEAN,
    created_at               TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE company_billing_profiles
(
    id                  uuid                NOT NULL,
    user_id             uuid                NOT NULL,
    verification_status verification_status NOT NULL,
    name                TEXT,
    registration_number TEXT,
    registration_date   TIMESTAMP,
    address             TEXT,
    country             TEXT,
    us_entity           BOOLEAN,
    subject_to_eu_vat   BOOLEAN,
    eu_vat_number       TEXT,
    created_at          TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE user_billing_profile_types
(
    user_id              uuid                 NOT NULL,
    billing_profile_type billing_profile_type NOT NULL,
    created_at           TIMESTAMP            NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP            NOT NULL DEFAULT NOW()
);

insert into user_billing_profile_types (user_id, billing_profile_type)
select upi.user_id, 'INDIVIDUAL'
from user_payout_info upi
where upi.identity -> 'Person' is not null;

insert into user_billing_profile_types (user_id, billing_profile_type)
select upi.user_id, 'COMPANY'
from user_payout_info upi
where upi.identity -> 'Company' is not null;

with individual_profile
         as (select upi.identity -> 'Person' ->> 'firstname' first_name,
                    upi.identity -> 'Person' ->> 'lastname'  last_name,
                    upi.location ->> 'city'                  city,
                    upi.location ->> 'address'               address,
                    upi.location ->> 'country'               country,
                    upi.location ->> 'post_code'             post_code,
                    upi.user_id
             from user_payout_info upi
             where upi.identity -> 'Person' is not null)
insert
into individual_billing_profiles (id, user_id, verification_status, first_name, last_name, country, address)
select gen_random_uuid(),
       ip.user_id,
       'NOT_STARTED',
       ip.first_name,
       ip.last_name,
       ip.country,
       ip.address || ', ' || ip.post_code || ', ' || ip.city || ', ' || ip.country
from individual_profile ip;



with company_profile
         as (select upi.identity -> 'Company' ->> 'name'                  name,
                    upi.identity -> 'Company' ->> 'identification_number' id_number,
                    upi.location ->> 'city'                               city,
                    upi.location ->> 'address'                            address,
                    upi.location ->> 'country'                            country,
                    upi.location ->> 'post_code'                          post_code,
                    upi.user_id
             from user_payout_info upi
             where upi.identity -> 'Company' is not null)
insert
into company_billing_profiles (id, user_id, verification_status, name, registration_number, country, address)
select gen_random_uuid(),
       cp.user_id,
       'NOT_STARTED',
       cp.name,
       cp.id_number,
       cp.country,
       cp.address || ', ' || cp.post_code || ', ' || cp.city || ', ' || cp.country
from company_profile cp;