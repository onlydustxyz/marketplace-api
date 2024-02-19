CREATE TYPE invoice_status AS ENUM ('DRAFT', 'PROCESSING', 'REJECTED', 'APPROVED');


CREATE TABLE accounting.invoices
(
    id                 UUID PRIMARY KEY NOT NULL,
    billing_profile_id UUID             NOT NULL,
    number             TEXT             NOT NULL,
    created_at         TIMESTAMP        NOT NULL,
    status             invoice_status   NOT NULL,
    amount             DECIMAL          NOT NULL,
    currency_id        UUID             NOT NULL REFERENCES currencies (id),
    url                TEXT,
    data               JSONB            NOT NULL,
    tech_created_at    TIMESTAMP        NOT NULL DEFAULT now(),
    tech_updated_at    TIMESTAMP        NOT NULL DEFAULT now(),
    UNIQUE (billing_profile_id, number)
);

CREATE TRIGGER accounting_invoices_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.invoices
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();


ALTER TABLE payment_requests
    ADD COLUMN invoice_id UUID REFERENCES accounting.invoices (id);

