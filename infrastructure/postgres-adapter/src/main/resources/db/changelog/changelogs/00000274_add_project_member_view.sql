CREATE VIEW project_members AS
SELECT pc.project_id     as project_id,
       pc.github_user_id as github_user_id,
       u.id              as user_id
FROM projects_contributors pc
         LEFT JOIN iam.users u on u.github_user_id = pc.github_user_id
UNION
SELECT pl.project_id    as project_id,
       u.github_user_id as github_user_id,
       pl.user_id       as user_id
FROM project_leads pl
         LEFT JOIN iam.users u on u.id = pl.user_id;
