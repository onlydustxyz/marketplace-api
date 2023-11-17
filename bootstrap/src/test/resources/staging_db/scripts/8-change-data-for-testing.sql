update project_details
set visibility = 'PRIVATE'
where project_id = '27ca7e18-9e71-468f-8825-c64fe6b79d66'; -- B Conseil

UPDATE indexer_exp.contributions
SET github_status = 'DRAFT'
WHERE id in ('b66cd16a35e0043d86f1850eb9ba6519d20ff833394f7516b0842fa2f18a5abf',
             '7b076143d6844660494a112d2182017a367914577b14ed562250ef1751de6547');

UPDATE indexer_exp.github_pull_requests
SET status = 'DRAFT'
WHERE repo_id = 493591124
  AND number = 17;

