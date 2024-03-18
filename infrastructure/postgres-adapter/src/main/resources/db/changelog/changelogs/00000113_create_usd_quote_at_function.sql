CREATE FUNCTION accounting.usd_quote_at(currency_id UUID, at timestamp)
    RETURNS NUMERIC AS
$$
SELECT price
FROM accounting.historical_quotes hq
         JOIN currencies usd ON usd.id = hq.target_id and usd.code = 'USD'
WHERE hq.base_id = currency_id
  AND hq.timestamp <= at
ORDER BY hq.timestamp DESC
LIMIT 1
$$ LANGUAGE SQL;
