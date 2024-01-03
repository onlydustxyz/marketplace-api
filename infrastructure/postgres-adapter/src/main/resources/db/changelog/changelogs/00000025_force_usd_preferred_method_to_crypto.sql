UPDATE public.user_payout_info
SET usd_preferred_method = 'crypto'
WHERE usd_preferred_method = 'fiat';
