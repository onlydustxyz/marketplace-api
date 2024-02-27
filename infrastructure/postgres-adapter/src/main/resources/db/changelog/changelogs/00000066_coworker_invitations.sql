ALTER TABLE accounting.billing_profiles_users
    DROP COLUMN invited_at;

CREATE TABLE accounting.billing_profiles_user_invitations
(
    billing_profile_id UUID                            NOT NULL REFERENCES accounting.billing_profiles (id),
    github_user_id     BIGINT                          NOT NULL,
    role               accounting.billing_profile_role NOT NULL,
    invited_at         TIMESTAMP                       NOT NULL,
    invited_by         UUID                            NOT NULL REFERENCES iam.users (id),
    tech_created_at    TIMESTAMP                       NOT NULL DEFAULT NOW(),
    tech_updated_at    TIMESTAMP                       NOT NULL DEFAULT NOW(),
    PRIMARY KEY (billing_profile_id, github_user_id)
);

CREATE TRIGGER accounting_bp_user_invitations_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.billing_profiles_user_invitations
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();