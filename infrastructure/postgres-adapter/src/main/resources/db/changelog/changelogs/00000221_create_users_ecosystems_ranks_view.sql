CREATE VIEW users_ecosystems_ranks AS
(
select pe.ecosystem_id                                                               as ecosystem_id,
       c.contributor_id                                                              as contributor_id,
       rank() over (partition by pe.ecosystem_id order by count(distinct c.id) desc) as rank
from projects_ecosystems pe
         join project_github_repos pgr on pe.project_id = pgr.project_id
         join projects p on p.id = pe.project_id and p.visibility = 'PUBLIC'
         join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id and gr.visibility = 'PUBLIC'
         join indexer_exp.contributions c on c.repo_id = pgr.github_repo_id
         join indexer_exp.github_accounts ga on ga.id = c.contributor_id and ga.type = 'USER'
group by pe.ecosystem_id, c.contributor_id
    );