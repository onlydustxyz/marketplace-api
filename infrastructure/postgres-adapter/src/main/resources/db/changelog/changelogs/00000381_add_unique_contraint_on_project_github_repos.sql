-- Add unique constraint, but only if there are no duplicates. This allows us to
-- add the constraint in develop|staging|production without having to clean up the IT dump.
DO
$$
    BEGIN
        -- Check for duplicates
        IF NOT EXISTS (SELECT github_repo_id
                       FROM project_github_repos
                       GROUP BY github_repo_id
                       HAVING COUNT(*) > 1) THEN
            -- Add unique constraint if no duplicates are found
            ALTER TABLE project_github_repos
                ADD CONSTRAINT unique_github_repo_id UNIQUE (github_repo_id);
        ELSE
            RAISE NOTICE 'Duplicate values found in github_repo_id column. Unique constraint not added.';
        END IF;
    END
$$;