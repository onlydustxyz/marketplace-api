INSERT INTO public.currencies (id, type, name, code, logo_url, decimals, description, country_restrictions, cmc_id)
VALUES (gen_random_uuid(), 'FIAT', 'US Dollar', 'USD', 'https://staging-onlydust-app-images.s3.eu-west-1.amazonaws.com/f171e9690f6658e106a049cd62843ec4.png', 2,
        null, '{}', 2781);
