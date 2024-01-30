create table ecosystems
(
    id       uuid default gen_random_uuid() not null primary key,
    name     text                           not null unique,
    logo_url text                           not null,
    url      text
);

create table projects_ecosystems
(
    project_id   uuid not null references projects on delete cascade,
    ecosystem_id uuid not null references ecosystems on delete cascade,
    primary key (project_id, ecosystem_id),
    unique (project_id, ecosystem_id)
);

INSERT INTO ecosystems (name, logo_url, url)
VALUES ('Zama', 'https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png', 'https://www.zama.ai/');
INSERT INTO ecosystems (name, logo_url, url)
VALUES ('Starknet', 'https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png', 'https://www.starknet.io/en');
INSERT INTO ecosystems (name, logo_url, url)
VALUES ('Aptos', 'https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png', 'https://aptosfoundation.org/');
INSERT INTO ecosystems (name, logo_url, url)
VALUES ('Ethereum', 'https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png', 'https://ethereum.foundation/');
INSERT INTO ecosystems (name, logo_url, url)
VALUES ('Lava ', 'https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg', 'https://www.lavanet.xyz/');
INSERT INTO ecosystems (name, logo_url, url)
VALUES ('Aztec', 'https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg', 'https://aztec.network/');
INSERT INTO ecosystems (name, logo_url, url)
VALUES ('Optimism', 'https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12058007825795511084.png', 'https://www.optimism.io/');
INSERT INTO ecosystems (name, logo_url, url)
VALUES ('Avail ', 'https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png', 'https://www.availproject.org/');

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Zama')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Zama';

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Starknet')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name in ('Starkware Exploration Team', 'Voyager', 'Starkware', 'Starknet Foundation', 'Kakarot', 'Kasar Labs');

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Ethereum')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name in ('Paradigm', 'Ethereum Foundation');

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Aptos')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Aptos Foundation';

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Aztec')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Aztec Protocol';

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Optimism')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Optimism';

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Avail')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Avail';


