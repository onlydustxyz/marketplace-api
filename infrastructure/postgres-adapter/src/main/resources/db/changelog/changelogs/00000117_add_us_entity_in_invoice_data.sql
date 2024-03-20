update accounting.invoices
set data = jsonb_set(data, '{billingProfileSnapshot, kycSnapshot, usCitizen}', kyc.us_citizen::text::jsonb, true)
from accounting.kyc
where kyc.billing_profile_id = (data #>> '{billingProfileSnapshot, id, uuid}')::uuid
  and data #> '{billingProfileSnapshot, kycSnapshot}' IS NOT NULL;

update accounting.invoices
set data = jsonb_set(data, '{billingProfileSnapshot, kybSnapshot, usEntity}', kyb.us_entity::text::jsonb, true)
from accounting.kyb
where kyb.billing_profile_id = (data #>> '{billingProfileSnapshot, id, uuid}')::uuid
  and data #> '{billingProfileSnapshot, kybSnapshot}' IS NOT NULL;
