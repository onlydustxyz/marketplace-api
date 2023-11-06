alter table project_details
    add column reward_ignore_pull_requests_by_default             bool not null default false,
    add column reward_ignore_issues_by_default                    bool not null default false,
    add column reward_ignore_code_reviews_by_default              bool not null default false,
    add column reward_ignore_contributions_before_date_by_default timestamp; -- NULL means do not ignore anything based on date