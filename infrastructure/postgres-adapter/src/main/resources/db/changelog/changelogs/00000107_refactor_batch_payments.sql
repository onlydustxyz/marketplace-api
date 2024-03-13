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

ALTER TABLE accounting.batch_payments
    ADD COLUMN total_amounts_per_currency JSONB;

UPDATE accounting.batch_payments
SET total_amounts_per_currency = ba.amounts
FROM (select ba.id,
             jsonb_agg(jsonb_build_object(
                     'amount', r.amount,
                     'currencyName', c.name,
                     'currencyCode', c.code,
                     'currencyLogoUrl', c.logo_url,
                     'dollarsEquivalent', rsd.amount_usd_equivalent
                       )) amounts
      from accounting.batch_payments ba
               join reward_to_batch_payment r2bp on r2bp.batch_payment_id = ba.id
               join rewards r on r.id = r2bp.reward_id
               join currencies c on c.id = r.currency_id
               join accounting.reward_status_data rsd on rsd.reward_id = r.id
      group by ba.id) AS ba
WHERE accounting.batch_payments.id = ba.id;

ALTER TABLE accounting.batch_payments
    ALTER COLUMN total_amounts_per_currency SET NOT NULL;