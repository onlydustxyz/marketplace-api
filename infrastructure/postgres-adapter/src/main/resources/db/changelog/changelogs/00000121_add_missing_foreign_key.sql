update rewards r
set billing_profile_id = null
where NOT EXISTS(SELECT 1 from accounting.billing_profiles bp where bp.id = r.billing_profile_id);

ALTER TABLE rewards
    ADD CONSTRAINT rewards_billing_profile_id_fkey FOREIGN KEY (billing_profile_id) REFERENCES accounting.billing_profiles (id);
