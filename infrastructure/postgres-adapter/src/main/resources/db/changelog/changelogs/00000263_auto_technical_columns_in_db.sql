-- mail_outbox_events
alter table accounting.mail_outbox_events
    rename column created_at to tech_created_at;

alter table accounting.mail_outbox_events
    rename column updated_at to tech_updated_at;

alter table accounting.mail_outbox_events
    alter column tech_updated_at set default now();

create trigger accounting_mail_outbox_events_set_tech_updated_at
    before update
    on accounting.mail_outbox_events
    for each row
execute function set_tech_updated_at();


-- billing_profile_verification_outbox_events
alter table accounting.billing_profile_verification_outbox_events
    rename column created_at to tech_created_at;

alter table accounting.billing_profile_verification_outbox_events
    rename column updated_at to tech_updated_at;

alter table accounting.billing_profile_verification_outbox_events
    alter column tech_updated_at set default now();

create trigger accounting_bbp_verification_outbox_events_set_tech_updated_at
    before update
    on accounting.billing_profile_verification_outbox_events
    for each row
execute function set_tech_updated_at();


-- node_guardians_rewards_boost_outbox_events
alter table node_guardians_rewards_boost_outbox_events
    rename column created_at to tech_created_at;

alter table node_guardians_rewards_boost_outbox_events
    rename column updated_at to tech_updated_at;

alter table node_guardians_rewards_boost_outbox_events
    alter column tech_updated_at set default now();

create trigger node_guardians_rewards_boost_outbox_events_set_tech_updated_at
    before update
    on node_guardians_rewards_boost_outbox_events
    for each row
execute function set_tech_updated_at();


-- indexer_outbox_events
alter table indexer_outbox_events
    rename column created_at to tech_created_at;

alter table indexer_outbox_events
    rename column updated_at to tech_updated_at;

alter table indexer_outbox_events
    alter column tech_updated_at set default now();

create trigger indexer_outbox_events_set_tech_updated_at
    before update
    on indexer_outbox_events
    for each row
execute function set_tech_updated_at();


-- notification_outbox_events
alter table notification_outbox_events
    rename column created_at to tech_created_at;

alter table notification_outbox_events
    rename column updated_at to tech_updated_at;

alter table notification_outbox_events
    alter column tech_updated_at set default now();

create trigger notification_outbox_events_set_tech_updated_at
    before update
    on notification_outbox_events
    for each row
execute function set_tech_updated_at();


-- mail_outbox_events
alter table mail_outbox_events
    rename column created_at to tech_created_at;

alter table mail_outbox_events
    rename column updated_at to tech_updated_at;

alter table mail_outbox_events
    alter column tech_updated_at set default now();

create trigger mail_outbox_events_set_tech_updated_at
    before update
    on mail_outbox_events
    for each row
execute function set_tech_updated_at();


-- tracking_outbox_events
alter table tracking_outbox_events
    rename column created_at to tech_created_at;

alter table tracking_outbox_events
    rename column updated_at to tech_updated_at;

alter table tracking_outbox_events
    alter column tech_updated_at set default now();

create trigger tracking_outbox_events_set_tech_updated_at
    before update
    on tracking_outbox_events
    for each row
execute function set_tech_updated_at();
