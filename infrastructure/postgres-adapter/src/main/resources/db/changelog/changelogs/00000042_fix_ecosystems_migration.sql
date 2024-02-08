INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Zama')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Zama'
on conflict (project_id, ecosystem_id) do nothing;

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Starknet')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name in ('Starkware Exploration Team', 'Voyager', 'Starkware', 'Starknet Foundation', 'Kakarot', 'Kasar Labs')
on conflict (project_id, ecosystem_id) do nothing;

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Ethereum')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name in ('Paradigm', 'Ethereum Foundation')
on conflict (project_id, ecosystem_id) do nothing;

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Aptos')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Aptos Foundation'
on conflict (project_id, ecosystem_id) do nothing;

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Aztec')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Aztec Protocol'
on conflict (project_id, ecosystem_id) do nothing;

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Optimism')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Optimism'
on conflict (project_id, ecosystem_id) do nothing;

INSERT INTO projects_ecosystems as pe (project_id, ecosystem_id)
select ps.project_id, (select e.id from ecosystems e where e.name = 'Avail')
from projects_sponsors ps
         join sponsors s on s.id = ps.sponsor_id
where s.name = 'Avail'
on conflict (project_id, ecosystem_id) do nothing;