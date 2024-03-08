UPDATE accounting.invoices
SET data =
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


