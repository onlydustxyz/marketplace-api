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