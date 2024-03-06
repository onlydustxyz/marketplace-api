DROP INDEX indexer_exp.github_pull_requests_id_as_text_index;
CREATE UNIQUE INDEX github_pull_requests_id_as_text_index ON indexer_exp.github_pull_requests ((id::text));

DROP INDEX indexer_exp.github_issues_id_as_text_index;
CREATE UNIQUE INDEX github_issues_id_as_text_index ON indexer_exp.github_issues ((id::text));

CREATE INDEX payment_requests_requested_at_asc_index ON payment_requests (requested_at ASC);
CREATE INDEX payment_requests_requested_at_desc_index ON payment_requests (requested_at DESC);
CREATE INDEX payments_processed_at_asc_index ON payments (processed_at ASC);
CREATE INDEX payments_processed_at_desc_index ON payments (processed_at DESC);
