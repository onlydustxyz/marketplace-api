DROP TABLE projects_contributors;
DROP TABLE projects_pending_contributors;


CREATE VIEW projects_pending_contributors AS
SELECT pgr.project_id                       AS project_id,
       rc.contributor_id                    AS github_user_id,
       sum(rc.completed_contribution_count) AS completed_contribution_count,
       sum(rc.total_contribution_count)     AS total_contribution_count
FROM indexer_exp.repos_contributors rc
         JOIN public.project_github_repos pgr on pgr.github_repo_id = rc.repo_id
GROUP BY pgr.project_id, rc.contributor_id;


CREATE VIEW projects_contributors AS
SELECT *
FROM projects_pending_contributors
WHERE completed_contribution_count > 0;


CREATE OR REPLACE VIEW registered_users AS
SELECT au.id,
       au.github_user_id,
       COALESCE(ga.login, au.login_at_signup)                                   AS login,
       COALESCE(ga.avatar_url, au.avatar_url_at_signup)                         AS avatar_url,
       COALESCE(ga.html_url, 'https://github.com/'::text || au.login_at_signup) AS html_url,
       au.email,
       au.last_seen,
       au.admin
FROM auth_users au
         LEFT JOIN indexer_exp.github_accounts ga ON ga.id = au.github_user_id;


ALTER TYPE public.contribution_type RENAME VALUE 'pull_request' TO 'PULL_REQUEST';
ALTER TYPE public.contribution_type RENAME VALUE 'code_review' TO 'CODE_REVIEW';
ALTER TYPE public.contribution_type RENAME VALUE 'issue' TO 'ISSUE';


INSERT INTO indexer.user_indexing_jobs (user_id)
SELECT github_user_id
FROM auth_users
ON CONFLICT DO NOTHING;


CREATE OR REPLACE FUNCTION public.insert_user_indexing_jobs_from_auth_users()
    RETURNS TRIGGER AS
$$
BEGIN
    -- Insert or update a row into table github_user_indexes when a row is inserted in auth.user_providers
    INSERT INTO indexer.user_indexing_jobs (user_id)
    VALUES (NEW.github_user_id)
    ON CONFLICT DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_user_indexing_jobs_from_auth_users_trigger
    AFTER INSERT
    ON auth_users
    FOR EACH ROW
EXECUTE FUNCTION public.insert_user_indexing_jobs_from_auth_users();