CREATE INDEX IF NOT EXISTS individual_billing_profiles_user_id_index ON individual_billing_profiles (user_id);
CREATE INDEX IF NOT EXISTS company_billing_profiles_user_id_index ON company_billing_profiles (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS projects_sponsors_sponsor_id_project_id_index ON projects_sponsors (sponsor_id, project_id);

ALTER TABLE user_billing_profile_types
    ADD CONSTRAINT user_billing_profile_types_pk PRIMARY KEY (user_id);

-- Good to know: it is possible in Postgres to create indexes on casted columns \o/
CREATE INDEX IF NOT EXISTS github_pull_requests_id_as_text_index ON indexer_exp.github_pull_requests ((id::text));
CREATE INDEX IF NOT EXISTS github_issues_id_as_text_index ON indexer_exp.github_issues ((id::text));
