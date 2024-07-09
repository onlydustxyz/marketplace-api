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
     similar_categories as (select p1.project_id                              as project1_id,
                                   p2.project_id                              as project2_id,
                                   array_agg(distinct p1.project_category_id) as similar_categories
                            from projects_project_categories p1
                                     join projects_project_categories p2
                                          on p1.project_category_id = p2.project_category_id and p1.project_id != p2.project_id
                            group by p1.project_id, p2.project_id),
     projects_similarity_ranks as (select coalesce(sl.project1_id, sc.project1_id, se.project1_id)                     as project1_id,
                                          coalesce(sl.project2_id, sc.project2_id, se.project2_id)                     as project2_id,
                                          rank() over (order by coalesce(array_length(sl.similar_languages, 1), 0))    as similar_language_count_rank,
                                          rank() over (order by coalesce(array_length(sc.similar_contributors, 1), 0)) as similar_contributor_count_rank,
                                          rank() over (order by coalesce(array_length(se.similar_ecosystems, 1), 0))   as similar_ecosystem_count_rank,
                                          rank() over (order by coalesce(array_length(sca.similar_categories, 1), 0))  as similar_category_count_rank,
                                          sl.similar_languages                                                         as similar_languages,
                                          sc.similar_contributors                                                      as similar_contributors,
                                          se.similar_ecosystems                                                        as similar_ecosystems,
                                          sca.similar_categories                                                       as similar_categories
                                   from similar_languages sl
                                            full outer join similar_contributors sc on sl.project1_id = sc.project1_id and
                                                                                       sl.project2_id = sc.project2_id
                                            full outer join similar_ecosystems se on se.project1_id = coalesce(sc.project1_id, sl.project1_id) and
                                                                                     se.project2_id = coalesce(sc.project2_id, sl.project2_id)
                                            full outer join similar_categories sca
                                                            on sca.project1_id = coalesce(sc.project1_id, sl.project1_id, se.project1_id) and
                                                               sca.project2_id = coalesce(sc.project2_id, sl.project2_id, se.project2_id))
select psr.*,
       rank() over ( partition by psr.project1_id
           order by psr.similar_contributor_count_rank +
                    psr.similar_ecosystem_count_rank +
                    psr.similar_language_count_rank +
                    psr.similar_category_count_rank) as rank,
       percent_rank() over ( partition by psr.project1_id
           order by psr.similar_contributor_count_rank +
                    psr.similar_ecosystem_count_rank +
                    psr.similar_language_count_rank +
                    psr.similar_category_count_rank) as rank_percentile
from projects_similarity_ranks psr;