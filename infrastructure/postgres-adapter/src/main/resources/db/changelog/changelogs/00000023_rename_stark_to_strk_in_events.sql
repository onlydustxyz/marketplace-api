update events
set payload = replace(payload::text, 'STARK', 'STRK')::jsonb;