CREATE OR REPLACE VIEW accounting.reward_usd_equivalent_data
            (reward_id, reward_created_at, reward_currency_id, kycb_verified_at, currency_quote_available_at,
             unlock_date, reward_amount)
AS
WITH currency_quote_available_at AS (SELECT hq.base_id,
                                            min(hq."timestamp") AS available_at
                                     FROM accounting.historical_quotes hq
                                              JOIN currencies usd ON usd.code = 'USD'::text AND usd.id = hq.target_id
                                     GROUP BY hq.base_id)
SELECT r.id               AS reward_id,
       r.requested_at     AS reward_created_at,
       r.currency_id      AS reward_currency_id,
       bp.tech_updated_at AS kycb_verified_at,
       cqa.available_at   AS currency_quote_available_at,
       rs.unlock_date,
       r.amount           AS reward_amount
FROM accounting.reward_status_data rs
         JOIN rewards r ON r.id = rs.reward_id
         LEFT JOIN iam.users u ON u.github_user_id = r.recipient_id
         LEFT JOIN accounting.invoices i ON i.id = r.invoice_id
         LEFT JOIN accounting.payout_preferences pp ON pp.project_id = r.project_id AND pp.user_id = u.id
         LEFT JOIN accounting.billing_profiles bp ON bp.id = coalesce(i.billing_profile_id, pp.billing_profile_id) AND
                                                     bp.verification_status = 'VERIFIED'::accounting.verification_status
         LEFT JOIN currency_quote_available_at cqa ON cqa.base_id = r.currency_id;