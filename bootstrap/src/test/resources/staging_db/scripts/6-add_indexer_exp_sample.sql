insert into indexer_exp.github_app_installations (id, account_id)
values (123456, 98735558);

INSERT INTO indexer_exp.github_repos
VALUES (380954304, 8642470, 'bretzel-app', 'https://github.com/gregcha/bretzel-app',
        '2021-11-23T19:51:19Z', null, 0, 0, '{
        "CSS": 318778,
        "SCSS": 98360,
        "HTML": 62170,
        "JavaScript": 58873
    }'::jsonb, true, null);


INSERT INTO indexer_exp.github_accounts
VALUES (14176906, 'paritytech', 'ORGANIZATION', 'https://github.com/paritytech',
        'https://avatars.githubusercontent.com/u/14176906?v=4', 'Paritytech');

INSERT INTO indexer_exp.github_repos
VALUES (137778655, 14176906, 'substrate-telemetry', 'https://github.com/paritytech/substrate-telemetry',
        '2023-07-04T12:48:59Z', 'Deoxys Telemetry service', 1234, 6, '{
        "Rust": 407376,
        "TypeScript": 189390,
        "CSS": 30141,
        "JavaScript": 3844,
        "Dockerfile": 1982,
        "Shell": 732,
        "HTML": 625
    }'::jsonb, false, null);

INSERT INTO indexer_exp.github_accounts
VALUES (119948009, 'KasarLabs', 'ORGANIZATION', 'https://github.com/KasarLabs',
        'https://avatars.githubusercontent.com/u/119948009?v=4', 'Kasar Labs');

INSERT INTO indexer_exp.github_repos
VALUES (659718526, 119948009, 'deoxys-telemetry', 'https://github.com/KasarLabs/deoxys-telemetry',
        '2023-07-04T12:48:59Z', 'Deoxys Telemetry service', 1, 0, '{
        "Rust": 407023,
        "TypeScript": 189275,
        "CSS": 31648,
        "JavaScript": 3844,
        "Dockerfile": 1982,
        "HTML": 739,
        "Shell": 732
    }'::jsonb, true, 137778655);


