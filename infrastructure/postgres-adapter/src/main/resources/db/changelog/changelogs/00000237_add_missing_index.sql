CREATE INDEX IF NOT EXISTS contributions_completed_at_created_at_idx ON indexer_exp.contributions (coalesce(completed_at, created_at));
CREATE INDEX IF NOT EXISTS contributions_completed_at_created_at_desc_idx ON indexer_exp.contributions (coalesce(completed_at, created_at) desc);
CREATE INDEX IF NOT EXISTS github_repos_id_visibility_idx ON indexer_exp.github_repos (id, visibility);
