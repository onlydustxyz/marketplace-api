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
)

