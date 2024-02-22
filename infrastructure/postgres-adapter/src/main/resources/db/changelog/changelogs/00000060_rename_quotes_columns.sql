ALTER TABLE accounting.historical_quotes
    RENAME COLUMN base_id to target_id;

ALTER TABLE accounting.historical_quotes
    RENAME COLUMN currency_id to base_id;
