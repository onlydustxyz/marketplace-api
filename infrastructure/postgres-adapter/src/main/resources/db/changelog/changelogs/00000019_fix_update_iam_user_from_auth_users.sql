CREATE OR REPLACE FUNCTION public.update_iam_user_from_auth_users()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE iam.users SET last_seen_at = coalesce(NEW.last_seen, now()) WHERE id = NEW.id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;