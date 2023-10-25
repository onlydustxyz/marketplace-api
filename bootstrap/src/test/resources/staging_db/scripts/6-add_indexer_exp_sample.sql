create schema indexer_exp;

create table indexer_exp.github_accounts
(
    id         BIGINT PRIMARY KEY,
    login      TEXT NOT NULL,
    type       TEXT NOT NULL,
    html_url   TEXT NOT NULL,
    avatar_url TEXT,
    name       text
);


create table indexer_exp.github_repos
(
    id          BIGINT PRIMARY KEY,
    owner_id    BIGINT    NOT NULL REFERENCES indexer_exp.github_accounts (id),
    name        TEXT      NOT NULL,
    html_url    TEXT      NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    description TEXT,
    stars_count BIGINT    NOT NULL,
    forks_count BIGINT    NOT NULL,
    languages   jsonb     not null default '{}'::jsonb,
    has_issues  boolean   not null default true,
    parent_id   bigint references indexer_exp.github_repos (id)
);


INSERT INTO indexer_exp.github_accounts
VALUES (98735558, 'onlydustxyz', 'Organization', 'https://github.com/onlydustxyz',
        'https://avatars.githubusercontent.com/u/98735558?v=4', 'OnlyDust');

INSERT INTO indexer_exp.github_repos
VALUES (498695724, 98735558, 'marketplace-frontend', 'https://github.com/onlydustxyz/marketplace-frontend',
        '2023-10-02T11:16:20Z', 'Contributions marketplace backend services', 15, 10, '{
        "JavaScript": 100,
        "TypeScript": 600
    }'::jsonb, true, null);

create table indexer_exp.github_app_installations
(
    id         bigint primary key,
    account_id bigint not null references indexer_exp.github_accounts (id)
);

insert into indexer_exp.github_app_installations (id, account_id)
values (123456, 98735558);

