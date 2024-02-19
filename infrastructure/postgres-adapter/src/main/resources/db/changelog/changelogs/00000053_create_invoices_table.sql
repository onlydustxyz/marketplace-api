CREATE TYPE invoice_status AS ENUM ('DRAFT', 'PROCESSING', 'REJECTED', 'APPROVED');


CREATE TABLE accounting.invoices
(
    id                 UUID PRIMARY KEY NOT NULL,
    billing_profile_id UUID             NOT NULL,
    name               TEXT             NOT NULL,
    created_at         TIMESTAMP        NOT NULL,
    due_at             TIMESTAMP        NOT NULL,
    status             invoice_status   NOT NULL,
    tax_rate           BIGINT           NOT NULL,
    url                TEXT,
    data               JSONB            NOT NULL,
    tech_created_at    TIMESTAMP        NOT NULL DEFAULT now(),
    tech_updated_at    TIMESTAMP        NOT NULL DEFAULT now(),
    UNIQUE (billing_profile_id, name)
);

CREATE TRIGGER accounting_invoices_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.invoices
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();


ALTER TABLE payment_requests
    ADD COLUMN invoice_id UUID REFERENCES accounting.invoices (id);

