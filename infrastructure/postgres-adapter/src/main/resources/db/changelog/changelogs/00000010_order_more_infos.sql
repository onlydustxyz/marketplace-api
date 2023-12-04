ALTER TABLE project_more_infos
    ADD COLUMN rank INTEGER NOT NULL DEFAULT 0;

UPDATE project_more_infos
SET rank = sub.rank
FROM (SELECT project_id, url, (row_number() over (partition by project_id)) - 1 as rank
      FROM project_more_infos) AS sub
WHERE project_more_infos.project_id = sub.project_id
  AND project_more_infos.url = sub.url;

ALTER TABLE project_more_infos
    ALTER COLUMN rank DROP DEFAULT;