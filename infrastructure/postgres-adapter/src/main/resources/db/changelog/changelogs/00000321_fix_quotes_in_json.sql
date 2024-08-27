update accounting.account_books_events
set payload = jsonb_set(payload, '{event, from, type}', '"PROGRAM"', false)
where payload #>> '{event, from, type}' = '"PROGRAM"';
