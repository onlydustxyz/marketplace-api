CREATE VIEW project_members AS
SELECT coalesce(pc.project_id, pl.project_id)        as project_id,
       coalesce(pc.github_user_id, u.github_user_id) as github_user_id,
       u.id                                          as user_id
FROM projects_contributors pc
         LEFT JOIN iam.users u on u.github_user_id = pc.github_user_id
         FULL JOIN project_leads pl on pl.project_id = pc.project_id and pl.user_id = u.id;
