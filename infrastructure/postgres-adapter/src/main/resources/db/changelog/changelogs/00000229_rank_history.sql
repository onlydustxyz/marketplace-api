CREATE TABLE historical_user_ranks
(
    github_user_id BIGINT    NOT NULL,
    rank           INT       NOT NULL,
    timestamp      TIMESTAMP NOT NULL,
    PRIMARY KEY (github_user_id, timestamp)
);