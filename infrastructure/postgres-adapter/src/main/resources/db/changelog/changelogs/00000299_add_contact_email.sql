ALTER TABLE user_profile_info
    ADD COLUMN contact_email TEXT;

UPDATE user_profile_info upi
SET contact_email = u.email
FROM iam.users u
WHERE u.id = upi.id;

ALTER TABLE user_profile_info
    ALTER COLUMN contact_email SET NOT NULL;