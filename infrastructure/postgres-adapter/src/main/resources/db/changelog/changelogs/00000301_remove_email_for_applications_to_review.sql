DELETE
FROM iam.user_notification_settings_channels
WHERE category = 'MAINTAINER_PROJECT_PROGRAM'
  AND channel = 'EMAIL';
