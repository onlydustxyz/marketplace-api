ALTER TABLE accounting.billing_profiles_users
    ADD COLUMN invited_at TIMESTAMP,
    ADD COLUMN joined_at  TIMESTAMP;