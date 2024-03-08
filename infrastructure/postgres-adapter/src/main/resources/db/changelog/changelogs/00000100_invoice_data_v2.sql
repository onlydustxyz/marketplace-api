ALTER TABLE accounting.invoices
    ADD COLUMN created_by UUID;

UPDATE accounting.invoices
SET created_by = (select bpu.user_id from accounting.billing_profiles_users bpu where bpu.billing_profile_id = billing_profile_id),
    data       =
        jsonb_build_object
        (
                'dueAt', data -> 'dueAt',
                'rewards', data -> 'rewards',
                'taxRate', data -> 'taxRate',
                'billingProfileSnapshot',
                jsonb_build_object(
                        'id', jsonb_build_object('uuid', billing_profile_id),
                        'type', case when data ?? 'companyInfo' then 'COMPANY' else 'INDIVIDUAL' end,
                        'wallets', data -> 'wallets',
                        'bankAccount', data -> 'bankAccount',
                        'kybSnapshot', data -> 'companyInfo',
                        'kycSnapshot', data -> 'personalInfo'
                )
        );

ALTER TABLE accounting.invoices
    ALTER COLUMN created_by SET NOT NULL;
