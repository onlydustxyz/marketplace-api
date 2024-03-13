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
