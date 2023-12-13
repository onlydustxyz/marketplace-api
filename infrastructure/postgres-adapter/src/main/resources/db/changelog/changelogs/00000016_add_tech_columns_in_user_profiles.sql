CREATE OR REPLACE FUNCTION set_tech_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.tech_updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

ALTER TABLE public.user_profile_info
    ADD COLUMN tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN tech_updated_at TIMESTAMP NOT NULL DEFAULT now();

CREATE TRIGGER public_user_profile_info_set_tech_updated_at
    BEFORE UPDATE
    ON
        public.user_profile_info
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

ALTER TABLE public.user_payout_info
    ADD COLUMN tech_created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN tech_updated_at TIMESTAMP NOT NULL DEFAULT now();

CREATE TRIGGER public_user_payout_info_set_tech_updated_at
    BEFORE UPDATE
    ON
        public.user_payout_info
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();