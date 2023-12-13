CREATE OR REPLACE VIEW public.project_technologies AS
SELECT pgr.project_id      as project_id,
       grl.language        as technology,
       sum(grl.line_count) as line_count
FROM project_github_repos pgr
         JOIN indexer_exp.github_repo_languages grl ON grl.repo_id = pgr.github_repo_id
         JOIN indexer_exp.github_repos gr on gr.id = pgr.github_repo_id AND gr.visibility = 'PUBLIC'
GROUP BY pgr.project_id, grl.language;

