CREATE TABLE custom_ignored_contributions
(
    project_id      uuid    NOT NULL,
    contribution_id text    NOT NULL,
    ignored         boolean NOT NULL,
    PRIMARY KEY (project_id, contribution_id)
);


insert into custom_ignored_contributions(project_id, contribution_id, ignored)
SELECT project_id, contribution_id, TRUE
FROM ignored_contributions;

