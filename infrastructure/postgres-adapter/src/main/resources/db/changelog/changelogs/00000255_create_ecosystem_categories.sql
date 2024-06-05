create view ecosystem_project_categories as
select distinct pe.ecosystem_id         as ecosystem_id,
                ppc.project_category_id as project_category_id
from projects_ecosystems pe
         join projects_project_categories ppc on ppc.project_id = pe.project_id;