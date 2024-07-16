create view hackathon_issues as
SELECT DISTINCT h.id                      as hackathon_id,
                i.id                      as issue_id,
                array_agg(pgr.project_id) as project_ids
FROM indexer_exp.github_issues i
         JOIN project_github_repos pgr ON pgr.github_repo_id = i.repo_id
         JOIN indexer_exp.github_issues_labels gil ON i.id = gil.issue_id
         JOIN indexer_exp.github_labels gl ON gil.label_id = gl.id
         JOIN hackathon_projects hp ON hp.project_id = pgr.project_id
         JOIN hackathons h ON gl.name = ANY (h.github_labels) AND hp.hackathon_id = h.id
GROUP BY h.id, i.id
;
