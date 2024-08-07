CREATE TYPE iam.new_notification_channel AS ENUM (
    'IN_APP',
    'EMAIL',
    'SUMMARY_EMAIL'
    );

ALTER TABLE iam.notification_channels
    ADD COLUMN new_channel iam.new_notification_channel;
ALTER TABLE iam.user_notification_settings_channels
    ADD COLUMN new_channel iam.new_notification_channel;

UPDATE iam.notification_channels
SET new_channel = channel::text::iam.new_notification_channel
WHERE channel in ('IN_APP', 'EMAIL');

UPDATE iam.notification_channels
SET new_channel = 'SUMMARY_EMAIL'
WHERE channel = 'DAILY_EMAIL';

ALTER TABLE iam.notification_channels
    ALTER COLUMN new_channel SET NOT NULL;

ALTER TABLE iam.notification_channels
    DROP COLUMN channel;

ALTER TABLE iam.notification_channels
    RENAME COLUMN new_channel TO channel;

UPDATE iam.user_notification_settings_channels
SET new_channel = channel::text::iam.new_notification_channel
WHERE channel in ('IN_APP', 'EMAIL');

UPDATE iam.user_notification_settings_channels
SET new_channel = 'SUMMARY_EMAIL'
WHERE channel = 'DAILY_EMAIL';

ALTER TABLE iam.user_notification_settings_channels
    ALTER COLUMN new_channel SET NOT NULL;

ALTER TABLE iam.user_notification_settings_channels
    DROP COLUMN channel;

ALTER TABLE iam.user_notification_settings_channels
    RENAME COLUMN new_channel TO channel;

DROP TYPE iam.notification_channel;
ALTER TYPE iam.new_notification_channel RENAME TO notification_channel;