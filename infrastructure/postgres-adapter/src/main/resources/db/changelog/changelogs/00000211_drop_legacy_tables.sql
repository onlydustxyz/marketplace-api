DROP TABLE
    auth_users,
    budgets,
    closing_issues,
    commands,
    contributions,
    crypto_usd_quotes,
    event_deduplications,
    events,
    github_issues,
    github_pull_request_commits,
    github_pull_request_indexes,
    github_pull_request_reviews,
    github_pull_requests,
    github_repo_indexes,
    github_repos,
    github_user_indexes,
    github_users,
    projects_budgets,
    projects,
    projects_rewarded_users,
    reward_to_batch_payment,
    unlock_op_on_projects,
    user_billing_profile_types,
    user_payout_info
;

-- Keep sensitive data for now
CREATE SCHEMA legacy;

ALTER TABLE payment_requests
    SET SCHEMA legacy;

ALTER TABLE payments
    SET SCHEMA legacy;

ALTER TABLE work_items
    SET SCHEMA legacy;

