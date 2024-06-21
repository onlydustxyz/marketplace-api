DROP VIEW application_rankings;

CREATE VIEW application_rankings AS
WITH ranks as (select a.id                                                                      as application_id,

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

                        left join indexer_exp.contributions in_progress_contribs
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
                        applied_on.pending_application_count_on_other_projects),
     scores as (select application_id                                  as application_id,
                       (1 - best_projects_similarity_percentile) * 100 as best_projects_similarity_score,
                       (1 - main_repo_language_user_percentile) * 100  as main_repo_language_user_score,
                       (1 - project_fidelity_percentile) * 100         as project_fidelity_score,
                       applied_project_count                           as applied_project_count,
                       pending_application_count_on_this_project       as pending_application_count_on_this_project,
                       pending_application_count_on_other_projects     as pending_application_count_on_other_projects,
                       (1 - availability_percentile) * 100             as availability_score,
                       contribution_in_progress_count                  as contribution_in_progress_count
                from ranks)
select application_id                                                                                                            as application_id,
       best_projects_similarity_score::int                                                                                       as best_projects_similarity_score,
       main_repo_language_user_score::int                                                                                        as main_repo_language_user_score,
       project_fidelity_score::int                                                                                               as project_fidelity_score,
       applied_project_count::int                                                                                                as applied_project_count,
       pending_application_count_on_this_project::int                                                                            as pending_application_count_on_this_project,
       pending_application_count_on_other_projects::int                                                                          as pending_application_count_on_other_projects,
       availability_score::int                                                                                                   as availability_score,
       contribution_in_progress_count::int                                                                                       as contribution_in_progress_count,
       ((availability_score + best_projects_similarity_score + main_repo_language_user_score + project_fidelity_score) / 4)::int as recommendation_score
from scores;
