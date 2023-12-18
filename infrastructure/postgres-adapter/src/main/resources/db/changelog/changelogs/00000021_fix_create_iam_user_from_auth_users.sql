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
         LEFT JOIN indexer_exp.github_accounts ga ON ga.id = au.github_user_id
ON CONFLICT (id) DO NOTHING;


CREATE OR REPLACE FUNCTION public.insert_iam_user_from_auth_users()
    RETURNS TRIGGER AS
$$
BEGIN
    IF (NEW.github_user_id IS NOT NULL) THEN
        INSERT INTO iam.users (id, github_user_id, github_login, github_avatar_url, email, roles, created_at)
        SELECT NEW.id,
               NEW.github_user_id,
               coalesce((SELECT ga.login FROM indexer_exp.github_accounts ga WHERE ga.id = NEW.github_user_id),
                        NEW.login_at_signup),
               coalesce((SELECT ga.avatar_url FROM indexer_exp.github_accounts ga WHERE ga.id = NEW.github_user_id),
                        NEW.avatar_url_at_signup),
               coalesce(NEW.email, ''),
               CASE WHEN NEW.admin is true THEN '{USER, ADMIN}'::iam.user_role[] ELSE '{USER}'::iam.user_role[] END,
               coalesce(NEW.created_at, now())
        ON CONFLICT (id) DO NOTHING;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE TRIGGER insert_iam_user_from_auth_users_trigger
    AFTER INSERT OR UPDATE OF github_user_id
    ON auth_users
    FOR EACH ROW
EXECUTE FUNCTION public.insert_iam_user_from_auth_users();

CREATE OR REPLACE FUNCTION public.update_iam_user_email_from_auth_users()
    RETURNS TRIGGER AS
$$
BEGIN
    IF (NEW.email IS NOT NULL) THEN
        UPDATE iam.users SET email = NEW.email WHERE id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER update_iam_user_email_from_auth_users_trigger
    AFTER UPDATE OF email
    ON auth_users
    FOR EACH ROW
EXECUTE FUNCTION public.update_iam_user_email_from_auth_users();

CREATE OR REPLACE TRIGGER update_iam_user_from_auth_users_trigger
    AFTER UPDATE OF last_seen
    ON auth_users
    FOR EACH ROW
EXECUTE FUNCTION public.update_iam_user_from_auth_users();

CREATE OR REPLACE TRIGGER update_iam_user_roles_from_auth_users_trigger
    AFTER UPDATE OF admin
    ON auth_users
    FOR EACH ROW
EXECUTE FUNCTION public.update_iam_user_roles_from_auth_users();