CREATE OR REPLACE VIEW completed_contributions AS
select c.*,
       array_agg(distinct p.id) as project_ids
from indexer_exp.contributions c
         join project_github_repos pgr on pgr.github_repo_id = c.repo_id
         join projects p on p.id = pgr.project_id
where c.status = 'COMPLETED'
group by c.id
;



create materialized view bi.program_contribution_stats as
select pp.program_id        as program_id,
       count(distinct u.id) as user_count
from programs_projects pp
         join completed_contributions c on pp.project_id = ANY (c.project_ids)
         left join project_leads pl on pl.project_id = pp.project_id
         left join lateral ( select distinct u.id
                             from iam.users u
                             where u.github_user_id = c.contributor_id
                                or u.id = pl.user_id
    ) u on true
group by pp.program_id;

create unique index program_contribution_stats_pk on bi.program_contribution_stats (program_id);
refresh materialized view bi.program_contribution_stats;



create or replace view bi.program_stats as
select p.id                                      as program_id,
       coalesce(count(distinct s.project_id), 0) as granted_project_count,
       coalesce(pcs.user_count, 0)               as user_count_of_granted_projects
from programs p
         left join bi.program_stats_per_currency_per_project s on p.id = s.program_id and s.total_granted > 0
         left join bi.program_contribution_stats pcs on pcs.program_id = s.program_id
group by p.id, pcs.user_count;
