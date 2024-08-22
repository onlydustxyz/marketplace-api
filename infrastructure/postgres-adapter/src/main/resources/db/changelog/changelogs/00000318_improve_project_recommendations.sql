drop materialized view top_project_recommendations;

WITH user_languages AS (SELECT upi.id                             AS user_id,
                               unnest(upi.preferred_language_ids) AS language_id
                        FROM user_profile_info upi
                        UNION
                        SELECT u_1.id AS user_id,
                               stats.language_id
                        FROM contributions_stats_per_language_per_user stats
                                 JOIN iam.users u_1 ON u_1.github_user_id = stats.contributor_id),
     user_categories AS (SELECT upi.id                             AS user_id,
                                unnest(upi.preferred_category_ids) AS category_id
                         FROM user_profile_info upi),
     similar_languages AS (SELECT pl.project_id,
                                  ul.user_id,
                                  array_agg(ul.language_id) AS languages,
                                  count(ul.language_id)     AS language_count
                           FROM user_languages ul
                                    LEFT JOIN project_languages pl ON pl.language_id = ul.language_id
                           GROUP BY pl.project_id, ul.user_id),
     similar_categories AS (SELECT pc.project_id,
                                   uc.user_id                AS user_id,
                                   array_agg(uc.category_id) AS categories,
                                   count(uc.category_id)     AS category_count
                            FROM user_categories uc
                                     LEFT JOIN projects_project_categories pc ON pc.project_category_id = uc.category_id
                            GROUP BY pc.project_id, uc.user_id)
SELECT u.id                                                                                                      AS user_id,
       u.github_user_id,
       p.id                                                                                                      AS project_id,
       COALESCE(sl.languages, '{}'::uuid[])                                                                      AS similar_languages,
       COALESCE(sl.language_count, 0::bigint)                                                                    AS similar_language_count,
       COALESCE(sc.categories, '{}'::uuid[])                                                                     AS similar_categories,
       COALESCE(sc.category_count, 0::bigint)                                                                    AS similar_category_count,
       100000 * COALESCE(sl.language_count, 0::bigint) + 10000 * COALESCE(sc.category_count, 0::bigint) + p.rank AS score
FROM iam.users u
         CROSS JOIN projects p
         LEFT JOIN similar_languages sl ON sl.project_id = p.id AND sl.user_id = u.id
         LEFT JOIN similar_categories sc ON sc.project_id = p.id AND sc.user_id = u.id
WHERE NOT (EXISTS (SELECT 1
                   FROM projects_contributors pc
                   WHERE pc.project_id = p.id
                     AND pc.github_user_id = u.github_user_id))
  AND NOT (EXISTS (SELECT 1
                   FROM projects_good_first_issues pgfi
                   WHERE pgfi.project_id = p.id));

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
