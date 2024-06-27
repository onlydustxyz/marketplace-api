create index if not exists github_issues_repo_id_index
    on indexer_exp.github_issues (repo_id);

create index if not exists github_issues_status_index
    on indexer_exp.github_issues (status);

CREATE EXTENSION IF NOT EXISTS pg_trgm;
create index if not exists github_labels_name_ginindex
    on indexer_exp.github_labels using gin (lower(name) gin_trgm_ops);

