ALTER TABLE accounting.reward_status_data
    ADD COLUMN usd_conversion_rate NUMERIC;

UPDATE accounting.reward_status_data
SET usd_conversion_rate = ucr.usd_conversion_rate
FROM (SELECT rsd.reward_id, rsd.amount_usd_equivalent / r.amount AS usd_conversion_rate
      FROM accounting.reward_status_data rsd
               JOIN rewards r ON rsd.reward_id = r.id AND r.amount != 0
      WHERE rsd.usd_conversion_rate IS NULL
        AND rsd.amount_usd_equivalent IS NOT NULL) AS ucr
WHERE accounting.reward_status_data.reward_id = ucr.reward_id;


CREATE TABLE accounting.batch_payment_rewards
(
    batch_payment_id UUID      NOT NULL REFERENCES accounting.batch_payments (id),
    reward_id        UUID      NOT NULL REFERENCES rewards (id),
    amount           NUMERIC   NOT NULL,
    tech_created_at  TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (batch_payment_id, reward_id)
);

CREATE TRIGGER batch_payment_rewards_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.batch_payment_rewards
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

INSERT INTO accounting.batch_payment_rewards (batch_payment_id, reward_id, amount)
SELECT r2bp.batch_payment_id, r2bp.reward_id, r.amount
FROM reward_to_batch_payment r2bp
         JOIN rewards r ON r2bp.reward_id = r.id;

CREATE TABLE accounting.batch_payment_invoices
(
    batch_payment_id UUID      NOT NULL REFERENCES accounting.batch_payments (id),
    invoice_id       UUID      NOT NULL REFERENCES accounting.invoices (id),
    tech_created_at  TIMESTAMP NOT NULL DEFAULT now(),
    tech_updated_at  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (batch_payment_id, invoice_id)
);

CREATE TRIGGER batch_payment_rewards_set_tech_updated_at
    BEFORE UPDATE
    ON accounting.batch_payment_invoices
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

INSERT INTO accounting.batch_payment_invoices (batch_payment_id, invoice_id)
SELECT r2bp.batch_payment_id, r.invoice_id
FROM reward_to_batch_payment r2bp
         JOIN rewards r ON r2bp.reward_id = r.id
WHERE r.invoice_id IS NOT NULL;

ALTER TABLE rewards
    ADD COLUMN payment_notified_at TIMESTAMP;

UPDATE rewards
SET payment_notified_at = pr.payment_notified_at
FROM payment_requests pr
WHERE rewards.id = pr.id;
