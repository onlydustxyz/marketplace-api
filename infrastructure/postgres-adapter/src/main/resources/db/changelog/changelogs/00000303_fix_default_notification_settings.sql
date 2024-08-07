INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'MAINTAINER_PROJECT_PROGRAM', 'EMAIL'
FROM iam.users;

DELETE
FROM iam.user_notification_settings_channels
WHERE category = 'MAINTAINER_PROJECT_CONTRIBUTOR'
  AND channel = 'EMAIL';
