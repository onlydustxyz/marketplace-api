-- EMAIL
INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'MAINTAINER_PROJECT_CONTRIBUTOR', 'EMAIL'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'MAINTAINER_PROJECT_PROGRAM', 'EMAIL'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'CONTRIBUTOR_REWARD', 'EMAIL'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'CONTRIBUTOR_PROJECT', 'EMAIL'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'KYC_KYB_BILLING_PROFILE', 'EMAIL'
FROM iam.users;

-- IN_APP
INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'MAINTAINER_PROJECT_CONTRIBUTOR', 'IN_APP'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'MAINTAINER_PROJECT_PROGRAM', 'IN_APP'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'CONTRIBUTOR_REWARD', 'IN_APP'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'CONTRIBUTOR_PROJECT', 'IN_APP'
FROM iam.users;

INSERT INTO iam.user_notification_settings_channels (user_id, category, channel)
SELECT id, 'KYC_KYB_BILLING_PROFILE', 'IN_APP'
FROM iam.users;