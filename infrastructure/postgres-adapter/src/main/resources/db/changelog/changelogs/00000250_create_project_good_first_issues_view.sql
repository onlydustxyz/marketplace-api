create view projects_good_first_issues as
SELECT DISTINCT pgr.project_id as project_id,
                i.id           as issue_id
FROM project_github_repos pgr
         join indexer_exp.github_issues i on i.repo_id = pgr.github_repo_id
         LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
         JOIN indexer_exp.github_issues_labels gil ON i.id = gil.issue_id
         JOIN indexer_exp.github_labels gl on gil.label_id = gl.id AND gl.name ilike '%good%first%issue%'
WHERE i.status = 'OPEN'
  AND gia.user_id IS NULL
;
