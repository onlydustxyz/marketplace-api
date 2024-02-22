CREATE TYPE accounting.verification_status AS ENUM ('NOT_STARTED','STARTED','UNDER_REVIEW','VERIFIED','REJECTED','CLOSED');
CREATE TYPE accounting.id_document_type AS ENUM ('PASSPORT','ID_CARD','RESIDENCE_PERMIT','DRIVER_LICENSE');
CREATE TABLE accounting.kyc
(
    id                       uuid                           NOT NULL PRIMARY KEY,
    owner_id                 uuid                           NOT NULL,
    billing_profile_id       uuid                           NOT NULL,
    verification_status      accounting.verification_status NOT NULL,
    first_name               TEXT,
    last_name                TEXT,
    address                  TEXT,
    country                  TEXT,
    birthdate                TIMESTAMP,
    valid_until              TIMESTAMP,
    id_document_number       TEXT,
    id_document_type         accounting.id_document_type,
    id_document_country_code TEXT,
    us_citizen               BOOLEAN,
    review_message           TEXT,
    applicant_id             TEXT,
    tech_created_at          TIMESTAMP                      NOT NULL DEFAULT NOW(),
    tech_updated_at          TIMESTAMP                      NOT NULL DEFAULT NOW()
);

CREATE TABLE accounting.kyb
(
    id                  uuid                           NOT NULL PRIMARY KEY,
    owner_id            uuid                           NOT NULL,
    billing_profile_id  uuid                           NOT NULL,
    verification_status accounting.verification_status NOT NULL,
    name                TEXT,
    registration_number TEXT,
    registration_date   TIMESTAMP,
    address             TEXT,
    country             TEXT,
    us_entity           BOOLEAN,
    subject_to_eu_vat   BOOLEAN,
    eu_vat_number       TEXT,
    review_message      TEXT,
    applicant_id        TEXT,
    tech_created_at     TIMESTAMP                      NOT NULL DEFAULT NOW(),
    tech_updated_at     TIMESTAMP                      NOT NULL DEFAULT NOW()
);

CREATE TYPE accounting.billing_profile_type AS ENUM ('INDIVIDUAL','COMPANY', 'SELF_EMPLOYED');

CREATE TABLE accounting.billing_profiles
(
    id                          uuid                            NOT NULL PRIMARY KEY,
    name                        TEXT                            NOT NULL,
    type                        accounting.billing_profile_type NOT NULL,
    invoice_mandate_accepted_at TIMESTAMP,
    tech_created_at             TIMESTAMP                       NOT NULL DEFAULT NOW(),
    tech_updated_at             TIMESTAMP                       NOT NULL DEFAULT NOW()
);

CREATE TYPE accounting.billing_profile_role AS ENUM ('ADMIN','MEMBER');

CREATE TABLE accounting.billing_profiles_users
(
    billing_profile_id uuid                            NOT NULL,
    user_id            uuid                            NOT NULL,
    role               accounting.billing_profile_role NOT NULL,
    tech_created_at    TIMESTAMP                       NOT NULL DEFAULT NOW(),
    tech_updated_at    TIMESTAMP                       NOT NULL DEFAULT NOW(),
    constraint billing_profile_id_user_id PRIMARY KEY (billing_profile_id, user_id)
)