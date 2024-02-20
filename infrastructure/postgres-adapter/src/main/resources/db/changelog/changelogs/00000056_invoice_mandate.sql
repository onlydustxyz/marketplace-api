ALTER TABLE global_settings
    ADD COLUMN invoice_mandate_latest_version_date TIMESTAMP;

UPDATE global_settings
SET invoice_mandate_latest_version_date = '2024-02-20 00:00:00'
WHERE id = 1;

ALTER TABLE global_settings
    ALTER COLUMN invoice_mandate_latest_version_date SET NOT NULL;

ALTER TABLE company_billing_profiles
    ADD COLUMN invoice_mandate_accepted_at TIMESTAMP;

ALTER TABLE individual_billing_profiles
    ADD COLUMN invoice_mandate_accepted_at TIMESTAMP;
