create index rewards_recipient_id_requested_at_index
    on rewards (recipient_id, requested_at);

drop index rewards_recipient_id_index;

create index on accounting.all_transactions (timestamp, sponsor_id, type, currency_id);
create index on accounting.all_transactions (timestamp, program_id, type, currency_id);
create index on accounting.all_transactions (timestamp, project_id, type, currency_id);
create index on accounting.all_transactions (timestamp, reward_id, type, currency_id);
create index on accounting.all_transactions (timestamp, type, currency_id);
create index on accounting.all_transactions (timestamp, type);
