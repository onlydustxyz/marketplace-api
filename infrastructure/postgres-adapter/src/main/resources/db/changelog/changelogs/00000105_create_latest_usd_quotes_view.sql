CREATE VIEW accounting.latest_usd_quotes AS
SELECT usd_quotes.base_id   AS currency_id,
       usd_quotes.price     AS price,
       usd_quotes.timestamp AS timestamp
FROM currencies c
         JOIN LATERAL ( SELECT *
                        FROM accounting.historical_quotes
                                 JOIN currencies usd ON usd.id = target_id AND usd.code = 'USD'
                        WHERE base_id = c.id
                        ORDER BY timestamp DESC
                        LIMIT 1
    ) usd_quotes ON TRUE
;