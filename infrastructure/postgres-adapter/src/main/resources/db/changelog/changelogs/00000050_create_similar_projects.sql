DROP MATERIALIZED VIEW IF EXISTS bi.m_project_similarities;

CREATE MATERIALIZED VIEW bi.m_project_similarities AS
WITH project_contributors AS (SELECT DISTINCT project_id,
                                              contributor_id
                              FROM bi.p_per_contributor_contribution_data
                              WHERE contributor_id IS NOT NULL)
SELECT p.project_id                           as project_id,
       p.project_slug                         as project_slug,
       sp.project_id                          as similar_project_id,
       sp.project                             as similar_project,
       sp.rank                                as similar_project_rank,

       -- Count matching ecosystems
       COALESCE(array_length(ARRAY(
                                     SELECT UNNEST(p.ecosystem_ids)
                                     INTERSECT
                                     SELECT UNNEST(sp.ecosystem_ids)
                             ), 1), 0)        as ecosystem_matches,

       -- Count matching languages
       COALESCE(array_length(ARRAY(
                                     SELECT UNNEST(p.language_ids)
                                     INTERSECT
                                     SELECT UNNEST(sp.language_ids)
                             ), 1), 0)        as language_matches,

       -- Count matching contributors
       (SELECT COUNT(DISTINCT pc1.contributor_id)
        FROM project_contributors pc1
                 JOIN project_contributors pc2
                      ON pc1.contributor_id = pc2.contributor_id
        WHERE pc1.project_id = p.project_id
          AND pc2.project_id = sp.project_id) as contributor_matches

FROM bi.p_project_global_data p
         CROSS JOIN bi.p_project_global_data sp
WHERE p.project_id != sp.project_id
  AND p.project_visibility = 'PUBLIC'
  AND sp.project_visibility = 'PUBLIC'
  AND (
    -- Only keep rows where there's at least one match in either ecosystems, languages or contributors
    COALESCE(array_length(ARRAY(SELECT UNNEST(p.ecosystem_ids)
                                INTERSECT
                                SELECT UNNEST(sp.ecosystem_ids)
                          ), 1), 0) > 0
        OR
    COALESCE(array_length(ARRAY(SELECT UNNEST(p.language_ids)
                                INTERSECT
                                SELECT UNNEST(sp.language_ids)
                          ), 1), 0) > 0
        OR EXISTS (SELECT 1
                   FROM project_contributors pc1
                            JOIN project_contributors pc2
                                 ON pc1.contributor_id = pc2.contributor_id
                   WHERE pc1.project_id = p.project_id
                     AND pc2.project_id = sp.project_id)
    );

CREATE UNIQUE INDEX ON bi.m_project_similarities (project_id, similar_project_id);
CREATE UNIQUE INDEX ON bi.m_project_similarities (project_slug, similar_project_id);
