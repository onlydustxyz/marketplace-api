create index if not exists notifications_data_notification_type_issue_id_index
    on iam.notifications ((data -> 'notification' ->> '@type'), (cast(data -> 'notification' -> 'issue' ->> 'id' as bigint)));

