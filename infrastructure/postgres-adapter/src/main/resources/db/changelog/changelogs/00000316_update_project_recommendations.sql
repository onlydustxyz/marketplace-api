drop materialized view top_project_recommendations;

create or replace view project_recommendations as
with user_languages as (select upi.id                             as user_id,
                               unnest(upi.preferred_language_ids) as language_id
                        from user_profile_info upi
                        union
                        select u.id            as user_id,
                               ulr.language_id as language_id
                        from users_languages_ranks ulr
                                 join iam.users u on u.github_user_id = ulr.contributor_id),
     similar_languages as (select pl.project_id             as project_id,
                                  ul.user_id                as user_id,
                                  array_agg(ul.language_id) as languages,
                                  count(ul.language_id)     as language_count
                           from user_languages ul
                                    left join project_languages pl on pl.language_id = ul.language_id
                           group by pl.project_id, ul.user_id),
     similar_categories as (select pc.project_id as project_id,
                                   upi.id        as user_id,
                                   array_agg(uc) as categories,
                                   count(uc)     as category_count
                            from user_profile_info upi,
                                 unnest(upi.preferred_category_ids) uc(uc)
                                     left join projects_project_categories pc on pc.project_category_id = uc
                            group by pc.project_id, upi.id),
     projects_with_gfi as (select distinct pgfi.project_id as project_id
                           from projects_good_first_issues pgfi)
select u.id                           as user_id,
       u.github_user_id               as github_user_id,
       p.id                           as project_id,
       coalesce(sl.languages, '{}')   as similar_languages,
       coalesce(sl.language_count, 0) as similar_language_count,
       coalesce(sc.categories, '{}')  as similar_categories,
       coalesce(sc.category_count, 0) as similar_category_count,
       100000 * coalesce(sl.language_count, 0)
           + 10000 * coalesce(sc.category_count, 0)
           + p.rank                   as score
from iam.users u
         cross join projects p
         join projects_with_gfi pgfi on pgfi.project_id = p.id
         left join similar_languages sl on sl.project_id = p.id and sl.user_id = u.id
         left join similar_categories sc on sc.project_id = p.id and sc.user_id = u.id
where not exists(select 1 from projects_contributors pc where pc.project_id = p.id and pc.github_user_id = u.github_user_id)
;

create materialized view top_project_recommendations as
select pr.user_id                      as user_id,
       pr.github_user_id               as github_user_id,
       pr.project_id                   as project_id,
       rank() over (order by random()) as rank
from project_recommendations pr
where pr.similar_category_count > 0
  and pr.similar_language_count > 0;

create unique index top_project_recommendations_github_user_id_project_id_idx
    on top_project_recommendations (github_user_id, project_id);

create unique index top_project_recommendations_user_id_project_id_idx
    on top_project_recommendations (user_id, project_id);

refresh materialized view top_project_recommendations;
