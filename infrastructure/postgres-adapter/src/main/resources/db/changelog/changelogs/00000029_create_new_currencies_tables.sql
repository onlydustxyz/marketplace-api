CREATE TYPE currency_type AS ENUM ('FIAT', 'CRYPTO');
CREATE TYPE currency_standard AS ENUM ('ISO4217', 'ERC20');

CREATE TABLE currencies
(
    id              UUID PRIMARY KEY,
    type            currency_type NOT NULL,
    standard        currency_standard,
    name            TEXT          NOT NULL,
    code            TEXT          NOT NULL,
    logo_url        TEXT          NOT NULL,
    decimals        INTEGER       NOT NULL,
    description     TEXT,
    tech_created_at TIMESTAMP     NOT NULL DEFAULT NOW(),
    tech_updated_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX currency_code_idx ON currencies (code);

CREATE TRIGGER update_currencies_tech_updated_at
    BEFORE UPDATE
    ON currencies
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

CREATE TABLE erc20
(
    blockchain      network   NOT NULL,
    address         TEXT      NOT NULL,
    name            TEXT      NOT NULL,
    symbol          TEXT      NOT NULL,
    decimals        INTEGER   NOT NULL,
    total_supply    NUMERIC   NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (blockchain, address)
);

CREATE UNIQUE INDEX erc20_blockchain_symbol_idx ON erc20 (blockchain, symbol);

CREATE TRIGGER update_erc20_tech_updated_at
    BEFORE UPDATE
    ON erc20
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();


CREATE TABLE quotes
(
    currency_id     UUID      NOT NULL REFERENCES currencies (id),
    base_id         UUID      NOT NULL REFERENCES currencies (id),
    price           NUMERIC   NOT NULL,
    tech_created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    tech_updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (currency_id, base_id)
);

CREATE TRIGGER update_quotes_tech_updated_at
    BEFORE UPDATE
    ON quotes
    FOR EACH ROW
EXECUTE PROCEDURE set_tech_updated_at();

-- TODO: remove
INSERT INTO currencies (id, type, standard, name, code, logo_url, decimals)
VALUES ('a03cdb25-abe9-494f-84d5-d7426a9bf4e0', 'FIAT', 'ISO4217', 'US Dollar', 'USD', 'https://s3.amazonaws.com/airswap-token-images/USD.png', 2);