CREATE OR REPLACE VIEW project_similarities AS
with similar_languages as (select p1.project_id                      as project1_id,
                                  p2.project_id                      as project2_id,
                                  array_agg(distinct p1.language_id) as similar_languages
                           from project_languages p1
                                    join project_languages p2
                                         on p1.language_id = p2.language_id and p1.project_id != p2.project_id
                           group by p1.project_id, p2.project_id),
     similar_contributors as (select p1.project_id                         as project1_id,
                                     p2.project_id                         as project2_id,
                                     array_agg(distinct p1.github_user_id) as similar_contributors
                              from projects_contributors p1
                                       join projects_contributors p2
                                            on p1.github_user_id = p2.github_user_id and p1.project_id != p2.project_id
                              group by p1.project_id, p2.project_id),
     similar_ecosystems as (select p1.project_id                       as project1_id,
                                   p2.project_id                       as project2_id,
                                   array_agg(distinct p1.ecosystem_id) as similar_ecosystems
                            from projects_ecosystems p1
                                     join projects_ecosystems p2
                                          on p1.ecosystem_id = p2.ecosystem_id and p1.project_id != p2.project_id
                            group by p1.project_id, p2.project_id),
     projects_similarity_ranks as (select coalesce(sl.project1_id, sc.project1_id, se.project1_id)              as project1_id,
                                          coalesce(sl.project2_id, sc.project2_id, se.project2_id)              as project2_id,
                                          rank()
                                          over (order by coalesce(array_length(sl.similar_languages, 1), 0))    as similar_language_count_rank,
                                          rank()
                                          over (order by coalesce(array_length(sc.similar_contributors, 1), 0)) as similar_contributor_count_rank,
                                          rank()
                                          over (order by coalesce(array_length(se.similar_ecosystems, 1), 0))   as similar_ecosystem_count_rank,
                                          sl.similar_languages                                                  as similar_languages,
                                          sc.similar_contributors                                               as similar_contributors,
                                          se.similar_ecosystems                                                 as similar_ecosystems
                                   from similar_languages sl
                                            full outer join similar_contributors sc
                                                            on sl.project1_id = sc.project1_id and sl.project2_id = sc.project2_id
                                            full outer join similar_ecosystems se on se.project1_id =
                                                                                     coalesce(sc.project1_id, sl.project1_id) and
                                                                                     se.project2_id =
                                                                                     coalesce(sc.project2_id, sl.project2_id))
select psr.*,
       rank() over ( partition by psr.project1_id
           order by psr.similar_contributor_count_rank +
                    psr.similar_ecosystem_count_rank +
                    psr.similar_language_count_rank) as rank,
       percent_rank() over ( partition by psr.project1_id
           order by psr.similar_contributor_count_rank +
                    psr.similar_ecosystem_count_rank +
                    psr.similar_language_count_rank) as rank_percentile
from projects_similarity_ranks psr;



CREATE MATERIALIZED VIEW public.repo_languages AS
select distinct c.repo_id                                                                 as repo_id,
                lfe.language_id                                                           as language_id,
                count(c.id)                                                               as contribution_count,
                rank() over (partition by c.repo_id order by count(c.id) desc nulls last) as rank
from indexer_exp.contributions c
         join language_file_extensions lfe ON lfe.extension = ANY (c.main_file_extensions)
group by c.repo_id, lfe.language_id;

CREATE UNIQUE INDEX repo_languages_pk
    ON repo_languages (repo_id, language_id);
REFRESH MATERIALIZED VIEW repo_languages;



CREATE MATERIALIZED VIEW application_rankings AS
select a.id                                                                      as application_id,

       coalesce(min(ps.rank_percentile), 1)                                      as best_projects_similarity_percentile,
       coalesce(min(ulr.rank_percentile), 1)                                     as main_repo_language_user_percentile,

       percent_rank() over (order by applied_on.project_count nulls first)       as project_fidelity_percentile,
       coalesce(applied_on.project_count, 0)                                     as applied_project_count,
       coalesce(applied_on.pending_application_count_on_this_project, 0)         as pending_application_count_on_this_project,
       coalesce(applied_on.pending_application_count_on_other_projects, 0)       as pending_application_count_on_other_projects,

       percent_rank() over (order by count(in_progress_contribs.id) nulls first) as availability_percentile,
       coalesce(count(in_progress_contribs.id), 0)                               as contribution_in_progress_count

from applications a
         join indexer_exp.github_issues gi on gi.id = a.issue_id
         left join repo_languages rl on rl.repo_id = gi.repo_id and rl.rank = 1
         left join users_languages_ranks ulr on ulr.contributor_id = a.applicant_id and ulr.language_id = rl.language_id

         left join projects_contributors pc on pc.project_id != a.project_id and pc.github_user_id = a.applicant_id
         left join project_similarities ps on ps.project1_id = a.project_id and ps.project2_id = pc.project_id

         join indexer_exp.contributions in_progress_contribs
              on in_progress_contribs.contributor_id = a.applicant_id and
                 in_progress_contribs.status = 'IN_PROGRESS'
         left join lateral (select count(distinct a2.project_id)                                        as project_count,
                                   count(distinct a2.id) filter ( where a2.project_id = a.project_id )  as pending_application_count_on_this_project,
                                   count(distinct a2.id) filter ( where a2.project_id != a.project_id ) as pending_application_count_on_other_projects
                            from applications a2
                                     left join indexer_exp.github_issues gi on gi.id = a2.issue_id
                                     left join indexer_exp.github_issues_assignees gia on gia.issue_id = gi.id
                            where a2.applicant_id = a.applicant_id
                              and gia.user_id is null) as applied_on on true
group by a.id,
         applied_on.project_count,
         applied_on.pending_application_count_on_this_project,
         applied_on.pending_application_count_on_other_projects;

CREATE UNIQUE INDEX application_rankings_pk ON application_rankings (id);
REFRESH MATERIALIZED VIEW application_rankings;