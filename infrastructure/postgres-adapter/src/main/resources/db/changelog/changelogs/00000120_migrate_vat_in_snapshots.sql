update accounting.invoices
set data = jsonb_set(data, '{billingProfileSnapshot, kybSnapshot, vatRegulationState}',
                     case
                         when data #>> '{billingProfileSnapshot, kybSnapshot, inEuropeanUnion}' = 'false'
                             then '"NOT_APPLICABLE_NON_UE"'::jsonb
                         when data #>> '{billingProfileSnapshot, kybSnapshot, isFrance}' = 'false'
                             then '"REVERSE_CHARGE"'::jsonb
                         when data #>> '{billingProfileSnapshot, kybSnapshot, subjectToEuVAT}' = 'false'
                             then '"NOT_APPLICABLE_FRENCH_NOT_SUBJECT"'::jsonb
                         else '"APPLICABLE"'::jsonb
                         end, true)
where data #> '{billingProfileSnapshot, kybSnapshot}' IS NOT NULL;

update accounting.invoices
set data = jsonb_set(data, '{billingProfileSnapshot, kybSnapshot, taxRate}',
                     case
                         when data #>> '{billingProfileSnapshot, kybSnapshot, vatRegulationState}' = 'APPLICABLE'
                             then '0.2'::jsonb
                         else '0'::jsonb
                         end, true)
where data #> '{billingProfileSnapshot, kybSnapshot}' IS NOT NULL;

