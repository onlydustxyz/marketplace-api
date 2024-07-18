create view hackathon_issue_counts as
SELECT hi.hackathon_id                                                               as hackathon_id,
       count(distinct i.id)                                                          as issue_count,
       count(distinct i.id) FILTER (WHERE i.status = 'OPEN' AND gia.user_id IS NULL) as open_issue_count
FROM hackathon_issues hi
         JOIN indexer_exp.github_issues i on i.id = hi.issue_id
         LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
GROUP BY hi.hackathon_id
;
