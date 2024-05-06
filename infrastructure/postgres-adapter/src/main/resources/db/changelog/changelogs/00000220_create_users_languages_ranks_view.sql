CREATE VIEW users_languages_ranks AS
(
select lfe.language_id                                                               as language_id,
       c.contributor_id                                                              as contributor_id,
       rank() over (partition by lfe.language_id order by count(distinct c.id) desc) as rank
from language_file_extensions lfe
         join indexer_exp.contributions c on lfe.extension = any (c.main_file_extensions)
         join indexer_exp.github_repos gr on gr.id = c.repo_id and gr.visibility = 'PUBLIC'
group by lfe.language_id, c.contributor_id
    );