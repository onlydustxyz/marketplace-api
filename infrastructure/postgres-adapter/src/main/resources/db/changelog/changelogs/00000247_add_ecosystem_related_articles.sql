CREATE TABLE ecosystem_articles
(
    id              UUID PRIMARY KEY,
    title           TEXT      NOT NULL,
    description     TEXT      NOT NULL,
    url             TEXT      NOT NULL,
    image_url       TEXT      NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TRIGGER ecosystem_articles_set_tech_updated_at
    BEFORE UPDATE
    ON ecosystem_articles
    FOR EACH ROW
EXECUTE FUNCTION set_tech_updated_at();

CREATE TABLE ecosystems_articles
(
    ecosystem_id    UUID      NOT NULL REFERENCES ecosystems (id),
    article_id      UUID      NOT NULL REFERENCES ecosystem_articles (id),
    tech_created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (ecosystem_id, article_id)
);

CREATE TRIGGER ecosystems_articles_set_tech_updated_at
    BEFORE UPDATE
    ON ecosystems_articles
    FOR EACH ROW
EXECUTE FUNCTION set_tech_updated_at();
