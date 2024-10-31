create unique index if not exists github_repo_languages_repo_id_line_count_language_uindex
    on indexer_exp.github_repo_languages (repo_id asc, line_count desc, language asc);

create unique index if not exists p_application_data_contributor_id_status_application_id_uindex
    on bi.p_application_data (contributor_id, status, application_id);

create unique index if not exists p_reward_data_contributor_id_reward_id_usd_amount_uindex
    on bi.p_reward_data (contributor_id, reward_id, usd_amount);

