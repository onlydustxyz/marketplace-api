CREATE TYPE iam.backoffice_user_role AS ENUM ('BO_ADMIN', 'BO_READER');

CREATE TABLE iam.backoffice_users
(
    id         UUID PRIMARY KEY,
    email      TEXT                       NOT NULL,
    name       TEXT                       NOT NULL,
    avatar_url TEXT,
    roles      iam.backoffice_user_role[] NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX backoffice_users_email_idx ON iam.backoffice_users (email);