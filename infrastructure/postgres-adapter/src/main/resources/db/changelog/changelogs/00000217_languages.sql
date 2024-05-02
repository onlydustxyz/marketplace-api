CREATE TABLE languages
(
    id              uuid PRIMARY KEY,
    name            text      NOT NULL,
    logo_url        text,
    banner_url      text,
    tech_created_at timestamp NOT NULL DEFAULT now(),
    tech_updated_at timestamp NOT NULL DEFAULT now()
);

CREATE TRIGGER languages_set_tech_updated_at
    BEFORE UPDATE
    ON languages
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE TABLE language_file_extensions
(
    extension       text PRIMARY KEY, -- An extension can only be associated with one language
    language_id     uuid      NOT NULL REFERENCES languages (id) ON DELETE CASCADE,
    tech_created_at timestamp NOT NULL DEFAULT now(),
    tech_updated_at timestamp NOT NULL DEFAULT now()
);

-- For index-only scans
CREATE UNIQUE INDEX language_file_extensions_extension_language_id_key
    ON language_file_extensions (extension, language_id);
CREATE UNIQUE INDEX language_file_extensions_language_id_extension_key
    ON language_file_extensions (language_id, extension);

CREATE TRIGGER language_file_extensions_set_tech_updated_at
    BEFORE UPDATE
    ON language_file_extensions
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();