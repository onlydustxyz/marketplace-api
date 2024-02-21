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
);

with individual_events as (select payload -> 'event' ->> 'applicantId'    applicant_id,
                                  payload -> 'event' ->> 'externalUserId' external_user_id
                           from user_verification_outbox_events event
                           where payload -> 'event' ->> 'applicantType' = 'individual')
update individual_billing_profiles
set applicant_id = individual_events.applicant_id
from individual_events
where id = cast(individual_events.external_user_id as uuid);

with company_events as (select payload -> 'event' ->> 'applicantId'    applicant_id,
                               payload -> 'event' ->> 'externalUserId' external_user_id
                        from user_verification_outbox_events event
                        where payload -> 'event' ->> 'applicantType' = 'company')
update company_billing_profiles
set applicant_id = company_events.applicant_id
from company_events
where id = cast(company_events.external_user_id as uuid);

update user_verification_outbox_events
set status = 'PENDING'
where payload -> 'event' ->> 'applicantType' = 'individual'
  and payload -> 'event' ->> 'applicantMemberOf' is not null
  and status = 'SKIPPED';





