create or replace function set_current_timestamp_updated_at() returns trigger
    language plpgsql
as
$$
declare _new record;
begin _new := new;
_new."updated_at" = now();
return _new;
end;
$$;

create or replace function set_tech_updated_at() returns trigger
    language plpgsql
as
$$
BEGIN
    NEW.tech_updated_at = now();
    RETURN NEW;
END;
$$;

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS citext;
