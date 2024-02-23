-- indexer_outbox_events
UPDATE indexer_outbox_events
SET payload = jsonb_set(payload #- '{notification}', '{event}', payload #> '{notification}')
WHERE payload ?? 'notification';

UPDATE indexer_outbox_events
SET payload = jsonb_set(payload #- '{event,className}',
                        '{event,@type}',
                        ('"' || split_part(payload #>> '{event,className}', '.', -1) || '"')::jsonb)
WHERE payload #> '{event}' ?? 'className';

ALTER TABLE indexer_outbox_events
    ADD COLUMN group_key TEXT;


-- notification_outbox_events
UPDATE notification_outbox_events
SET payload = jsonb_set(payload #- '{notification}', '{event}', payload #> '{notification}')
WHERE payload ?? 'notification';

UPDATE notification_outbox_events
SET payload = jsonb_set(payload #- '{event,className}',
                        '{event,@type}',
                        ('"' || split_part(payload #>> '{event,className}', '.', -1) || '"')::jsonb)
WHERE payload #> '{event}' ?? 'className';

ALTER TABLE notification_outbox_events
    ADD COLUMN group_key TEXT;


-- tracking_outbox_events
UPDATE tracking_outbox_events
SET payload = jsonb_set(payload #- '{notification}', '{event}', payload #> '{notification}')
WHERE payload ?? 'notification';

UPDATE tracking_outbox_events
SET payload = jsonb_set(payload #- '{event,className}',
                        '{event,@type}',
                        ('"' || split_part(payload #>> '{event,className}', '.', -1) || '"')::jsonb)
WHERE payload #> '{event}' ?? 'className';

ALTER TABLE tracking_outbox_events
    ADD COLUMN group_key TEXT;


-- user_verification_outbox_events
UPDATE user_verification_outbox_events
SET payload = jsonb_set(payload #- '{notification}', '{event}', payload #> '{notification}')
WHERE payload ?? 'notification';

UPDATE user_verification_outbox_events
SET payload = jsonb_set(payload #- '{event,className}',
                        '{event,@type}',
                        ('"' || split_part(payload #>> '{event,className}', '.', -1) || '"')::jsonb)
WHERE payload #> '{event}' ?? 'className';

ALTER TABLE user_verification_outbox_events
    ADD COLUMN group_key TEXT;
