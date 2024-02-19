ALTER TABLE company_billing_profiles
    ADD COLUMN applicant_id TEXT;
ALTER TABLE individual_billing_profiles
    ADD COLUMN applicant_id TEXT;

CREATE TABLE children_kyc
(
    applicant_id        text primary key,
    parent_applicant_id text                not null,
    verification_status verification_status not null,
    tech_created_at     TIMESTAMP           NOT NULL DEFAULT now(),
    tech_updated_at     TIMESTAMP           NOT NULL DEFAULT now()
)