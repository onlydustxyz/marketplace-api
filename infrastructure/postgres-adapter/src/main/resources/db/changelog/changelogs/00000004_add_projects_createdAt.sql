ALTER TABLE project_details
    add column created_at timestamp not null default now(),
    add column updated_at timestamp;

UPDATE project_details
SET created_at = e.timestamp
FROM (SELECT aggregate_id, timestamp
      FROM events
      WHERE aggregate_name = 'PROJECT'
        AND payload ?? 'Created') AS e
WHERE project_details.project_id = cast(e.aggregate_id as uuid);