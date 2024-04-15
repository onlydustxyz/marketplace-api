CREATE FUNCTION set_tech_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.tech_updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
