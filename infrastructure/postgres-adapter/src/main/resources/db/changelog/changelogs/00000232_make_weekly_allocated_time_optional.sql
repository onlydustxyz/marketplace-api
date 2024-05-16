ALTER TABLE user_profile_info
    ALTER COLUMN weekly_allocated_time DROP NOT NULL;

ALTER TABLE user_profile_info
    ALTER COLUMN weekly_allocated_time DROP DEFAULT;
