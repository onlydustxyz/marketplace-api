create index if not exists p_reward_data_contributor_id_project_id_index
    on bi.p_reward_data (contributor_id, project_id);

create index if not exists p_application_data_contributor_id_project_id_index
    on bi.p_application_data (contributor_id, project_id);

create index if not exists p_contributor_global_data_login_id_index
    on bi.p_contributor_global_data (contributor_login, contributor_id);

