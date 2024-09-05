INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'GLOBAL_MARKETING', 'EMAIL'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'GLOBAL_MARKETING', 'IN_APP'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'SPONSOR_LEAD', 'EMAIL'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'SPONSOR_LEAD', 'IN_APP'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'PROGRAM_LEAD', 'EMAIL'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'PROGRAM_LEAD', 'IN_APP'
FROM iam.users;
