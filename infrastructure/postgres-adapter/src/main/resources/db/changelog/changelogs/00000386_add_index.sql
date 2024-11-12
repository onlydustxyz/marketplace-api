create unique index p_contribution_data_project_id_is_gfi_activity_status_idx
    on bi.p_contribution_data (project_id, is_good_first_issue, activity_status, contribution_uuid);

create unique index p_per_contributor_cd_project_id_contributor_id_idx
    on bi.p_per_contributor_contribution_data (project_id, contributor_id, contribution_uuid);

create index p_reward_data_contributor_id_project_id_usd_amount_index
    on bi.p_reward_data (contributor_id, project_id, usd_amount);

