ALTER TABLE erc20
    ADD COLUMN currency_id UUID REFERENCES currencies (id);

UPDATE erc20
SET currency_id = currencies.id
FROM currencies
WHERE erc20.symbol = currencies.code;

ALTER TABLE erc20
    ALTER COLUMN currency_id SET NOT NULL;
