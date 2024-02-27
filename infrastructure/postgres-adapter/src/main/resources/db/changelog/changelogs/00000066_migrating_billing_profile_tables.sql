ALTER TABLE accounting.billing_profiles
    ADD COLUMN verification_status accounting.verification_status NOT NULL DEFAULT 'NOT_STARTED';
