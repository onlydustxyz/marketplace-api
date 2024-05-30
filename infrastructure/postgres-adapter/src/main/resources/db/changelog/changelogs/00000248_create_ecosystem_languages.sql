create view ecosystem_languages as
select distinct pe.ecosystem_id as ecosystem_id,
                pl.language_id  as language_id
from projects_ecosystems pe
         join project_languages pl on pe.project_id = pl.project_id;