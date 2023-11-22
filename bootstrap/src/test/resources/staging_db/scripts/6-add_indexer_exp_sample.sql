insert into indexer_exp.github_app_installations (id, account_id)
values (444555666, 8642470);

INSERT INTO indexer_exp.authorized_github_repos (repo_id, installation_id)
VALUES (380954304, 444555666),
       (452047076, 444555666);

UPDATE indexer_exp.contributions
SET github_status = 'DRAFT'
WHERE id in ('b66cd16a35e0043d86f1850eb9ba6519d20ff833394f7516b0842fa2f18a5abf',
             '7b076143d6844660494a112d2182017a367914577b14ed562250ef1751de6547');

UPDATE indexer_exp.github_pull_requests
SET status = 'DRAFT'
WHERE repo_id = 493591124
  AND number = 17;
