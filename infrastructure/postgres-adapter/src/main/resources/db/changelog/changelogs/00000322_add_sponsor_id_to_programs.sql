alter table programs
    add column sponsor_id uuid references sponsors (id);

update programs
set sponsor_id = s.id
from sponsors s
where programs.name = s.name;

alter table programs
    alter column sponsor_id set not null;

drop view sponsors_programs;