create materialized view if not exists public.project_languages as
select distinct pgr.project_id, lfe.language_id
from public_contributions c
         join project_github_repos pgr on pgr.github_repo_id = c.repo_id
         join language_file_extensions lfe ON lfe.extension = ANY (c.main_file_extensions);