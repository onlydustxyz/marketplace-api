update accounting.invoices
set data = jsonb_set(data, '{billingProfileSnapshot, kybSnapshot, vatRegulationState}',
                     case
                         when data #>> '{billingProfileSnapshot, kybSnapshot, inEuropeanUnion}' = 'false'
                             then 'NOT_APPLICABLE_NON_UE'
                         when data #>> '{billingProfileSnapshot, kybSnapshot, isFrance}' = 'false'
                             then 'REVERSE_CHARGE'
                         when data #>> '{billingProfileSnapshot, kybSnapshot, subjectToEuVAT}' = 'false'
                             then 'NOT_APPLICABLE_FRENCH_NOT_SUBJECT'
                         else 'APPLICABLE'
                         end, true)
from accounting.invoices
where data #> '{billingProfileSnapshot, kybSnapshot}' IS NOT NULL;

update accounting.invoices
set data = jsonb_set(data, '{billingProfileSnapshot, kybSnapshot, taxRate}',
                     case
                         when data #>> '{billingProfileSnapshot, kybSnapshot, vatRegulationState}' = 'APPLICABLE'
                             then 0.2
                         else 0
                         end, true)
from accounting.invoices
where data #> '{billingProfileSnapshot, kybSnapshot}' IS NOT NULL;

