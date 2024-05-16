ALTER TABLE user_profile_info
    ALTER COLUMN looking_for_a_job DROP NOT NULL;

ALTER TABLE user_profile_info
    ALTER COLUMN looking_for_a_job DROP DEFAULT;
