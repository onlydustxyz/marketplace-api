CREATE UNIQUE INDEX IF NOT EXISTS users_github_user_id_uindex
    ON iam.users (github_user_id);


-- add missing columns to IAM users table
ALTER TABLE iam.users
    ADD COLUMN email        TEXT,
    ADD COLUMN last_seen_at TIMESTAMP NOT NULL DEFAULT now();


-- Migrate data from auth_users to IAM users
INSERT INTO iam.users (id, github_user_id, github_login, github_avatar_url, email, roles, created_at, last_seen_at)
SELECT au.id,
       au.github_user_id,
       coalesce(ga.login, au.login_at_signup),
       coalesce(ga.avatar_url, au.avatar_url_at_signup),
       au.email,
       CASE WHEN au.admin is true THEN '{USER, ADMIN}'::iam.user_role[] ELSE '{USER}'::iam.user_role[] END,
       au.created_at,
       au.last_seen
FROM auth_users au
         LEFT JOIN indexer_exp.github_accounts ga ON ga.id = au.github_user_id;


-- Add missing constraints to IAM users table
ALTER TABLE iam.users
    ALTER COLUMN email SET NOT NULL;



-- Create a trigger to insert a row into table iam.users when a row is inserted in auth_users
CREATE OR REPLACE FUNCTION public.insert_iam_user_from_auth_users()
    RETURNS TRIGGER AS
$$
BEGIN
    INSERT INTO iam.users (id, github_user_id, github_login, github_avatar_url, email, roles, created_at)
    SELECT NEW.id,
           NEW.github_user_id,
           coalesce((SELECT ga.login FROM indexer_exp.github_accounts ga WHERE ga.id = NEW.github_user_id),
                    NEW.login_at_signup),
           coalesce((SELECT ga.avatar_url FROM indexer_exp.github_accounts ga WHERE ga.id = NEW.github_user_id),
                    NEW.avatar_url_at_signup),
           NEW.email,
           CASE WHEN NEW.admin is true THEN '{USER, ADMIN}'::iam.user_role[] ELSE '{USER}'::iam.user_role[] END,
           NEW.created_at;
    -- We do not add an ON CONFLICT clause because we want to FAIL if the user already
    -- exists in iam.users, as this should NEVER happen at this point.

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_iam_user_from_auth_users_trigger
    AFTER INSERT
    ON auth_users
    FOR EACH ROW
EXECUTE FUNCTION public.insert_iam_user_from_auth_users();



-- Create a trigger to update row in table iam.users when a row is updated in auth_users
CREATE OR REPLACE FUNCTION public.update_iam_user_from_auth_users()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE iam.users SET last_seen_at = NEW.last_seen WHERE id = NEW.id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_iam_user_from_auth_users_trigger
    AFTER UPDATE OF last_seen
    ON auth_users
    FOR EACH ROW
EXECUTE FUNCTION public.update_iam_user_from_auth_users();


-- Create a trigger to update row in table iam.users when 'admin' column is updated in auth_users
CREATE OR REPLACE FUNCTION public.update_iam_user_roles_from_auth_users()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE iam.users
    SET roles = CASE
                    WHEN NEW.admin is true THEN '{USER, ADMIN}'::iam.user_role[]
                    ELSE '{USER}'::iam.user_role[] END
    WHERE id = NEW.id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_iam_user_roles_from_auth_users_trigger
    AFTER UPDATE OF admin
    ON auth_users
    FOR EACH ROW
EXECUTE FUNCTION public.update_iam_user_roles_from_auth_users();