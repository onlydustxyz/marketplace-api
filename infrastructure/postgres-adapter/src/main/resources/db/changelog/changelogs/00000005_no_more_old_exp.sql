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