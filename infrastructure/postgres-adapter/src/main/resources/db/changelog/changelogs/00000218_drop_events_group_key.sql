ALTER TABLE accounting.billing_profile_verification_outbox_events
    DROP COLUMN group_key;

ALTER TABLE accounting.mail_outbox_events
    DROP COLUMN group_key;

ALTER TABLE public.node_guardians_rewards_boost_outbox_events
    DROP COLUMN group_key;

ALTER TABLE public.indexer_outbox_events
    DROP COLUMN group_key;

ALTER TABLE public.notification_outbox_events
    DROP COLUMN group_key;

ALTER TABLE public.tracking_outbox_events
    DROP COLUMN group_key;
