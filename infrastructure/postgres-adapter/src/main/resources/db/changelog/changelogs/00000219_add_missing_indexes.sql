create index if not exists reward_items_id_index
    on reward_items (id);

create index if not exists contributions_pull_request_id_as_text_index
    on indexer_exp.contributions (cast(pull_request_id as text));

create index if not exists contributions_code_review_id_as_text_index
    on indexer_exp.contributions (cast(code_review_id as text));

create index if not exists contributions_issue_id_as_text_index
    on indexer_exp.contributions (cast(issue_id as text));

create index if not exists github_code_reviews_pull_request_id_index
    on indexer_exp.github_code_reviews (pull_request_id);

drop index indexer_exp.github_pull_requests_closing_issues_issue_id_idx;
create unique index github_pull_requests_closing_issues_issue_id_idx
    on indexer_exp.github_pull_requests_closing_issues (issue_id, pull_request_id);

create index if not exists contributions_contributor_id_index
    on indexer_exp.contributions (contributor_id);

create index if not exists contributions_repo_id_index
    on indexer_exp.contributions (repo_id);

create index if not exists contributions_status_as_text_index
    on indexer_exp.contributions (status);

create index if not exists contributions_type_as_text_index
    on indexer_exp.contributions (type);

create index if not exists contributions_completed_at_index
    on indexer_exp.contributions (completed_at);

create index if not exists contributions_created_at_index
    on indexer_exp.contributions (created_at);



