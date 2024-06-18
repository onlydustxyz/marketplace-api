ALTER TABLE global_settings
    ADD COLUMN required_github_app_permissions text[];

UPDATE global_settings
SET required_github_app_permissions = '{issues:write,issues:read,metadata:read,pull_requests:read}'
WHERE id = 1;

ALTER TABLE global_settings
    ALTER COLUMN required_github_app_permissions SET NOT NULL;