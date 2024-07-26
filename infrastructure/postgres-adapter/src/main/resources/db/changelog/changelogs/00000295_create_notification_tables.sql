CREATE TYPE iam.notification_channel AS ENUM (
    'IN_APP',
    'EMAIL',
    'DAILY_EMAIL'
    );

CREATE TYPE iam.notification_category AS ENUM (
    'COMMITTEE_APPLICATION_AS_MAINTAINER',
    'REWARD_AS_CONTRIBUTOR',
    'PROJECT_APPLICATION_AS_CONTRIBUTOR',
    'PROJECT_APPLICATION_AS_MAINTAINER',
    'PROJECT_GOOD_FIRST_ISSUE_AS_CONTRIBUTOR'
    );

CREATE TYPE iam.notification_status AS ENUM (
    'PENDING',
    'SENT',
    'ERRORED'
    );

CREATE TABLE iam.user_notification_settings_channels
(
    user_id         UUID                      NOT NULL REFERENCES iam.users (id),
    category        iam.notification_category NOT NULL,
    channel         iam.notification_channel  NOT NULL,
    tech_created_at TIMESTAMP                 NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, category, channel)
);

CREATE TABLE iam.notifications
(
    id              UUID                      NOT NULL PRIMARY KEY,
    recipient_id    UUID                      NOT NULL REFERENCES iam.users (id),
    category        iam.notification_category NOT NULL,
    data            JSONB                     NOT NULL,
    created_at      TIMESTAMP                 NOT NULL,
    tech_created_at TIMESTAMP                 NOT NULL DEFAULT now()
);

CREATE TABLE iam.notification_channels
(
    notification_id UUID                     NOT NULL,
    channel         iam.notification_channel NOT NULL,
    sent_at         TIMESTAMP,
    tech_created_at TIMESTAMP                NOT NULL DEFAULT now(),
    PRIMARY KEY (notification_id, channel)
);