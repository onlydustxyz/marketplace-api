CREATE TABLE project_allowances
(
    project_id        UUID      NOT NULL REFERENCES projects (id),
    currency_id       UUID      NOT NULL REFERENCES currencies (id),
    current_allowance NUMERIC   NOT NULL,
    initial_allowance NUMERIC   NOT NULL,
    tech_created_at   TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at   TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (project_id, currency_id)
);

CREATE TRIGGER project_allowances_set_tech_updated_at
    BEFORE UPDATE
    ON public.project_allowances
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();
