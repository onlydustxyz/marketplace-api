create index if not exists notifications_data_notification_type_issue_id_index
    on iam.notifications ((data -> 'notification' ->> '@type'), (cast(data -> 'notification' -> 'issue' ->> 'id' as bigint)));

-- Not related to the feature, but recommended by Datadog
create index if not exists p_contribution_data_status_type_uuid_pid_pslug_rid_timestamp
    on bi.p_contribution_data (activity_status, contribution_type, contribution_uuid, project_id, project_slug, repo_id, timestamp);
