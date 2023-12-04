CREATE OR REPLACE FUNCTION public.insert_user_indexing_jobs_from_auth_users()
    RETURNS TRIGGER AS
$$
BEGIN
    -- Insert or update a row into table github_user_indexes when a row is inserted in auth.user_providers
    INSERT INTO indexer.user_indexing_jobs (user_id)
    VALUES (NEW.provider_user_id::bigint)
    ON CONFLICT DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER insert_user_indexing_jobs_from_auth_users_trigger
    AFTER INSERT
    ON auth.user_providers
    FOR EACH ROW
EXECUTE FUNCTION public.insert_user_indexing_jobs_from_auth_users();

DROP TRIGGER insert_user_indexing_jobs_from_auth_users_trigger ON auth_users;