CREATE TABLE archived_github_contributions
(
    id              BIGINT PRIMARY KEY,
    tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER archived_github_contributions_set_tech_updated_at
    BEFORE UPDATE
    ON archived_github_contributions
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

