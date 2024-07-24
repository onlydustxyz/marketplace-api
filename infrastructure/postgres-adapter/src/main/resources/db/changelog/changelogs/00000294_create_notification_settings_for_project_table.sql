CREATE TABLE iam.user_projects_notification_settings
(
    user_id                   UUID      NOT NULL REFERENCES iam.users (id),
    project_id                UUID      NOT NULL REFERENCES projects (id),
    on_good_first_issue_added BOOLEAN   NOT NULL,

    tech_created_at           TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at           TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, project_id)
);

CREATE TRIGGER user_projects_notification_settings_set_tech_updated_at
    BEFORE UPDATE
    ON iam.user_projects_notification_settings
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();