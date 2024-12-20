CREATE OR REPLACE PROCEDURE save_pseudo_projection_indexes_definition(schema text, name text)
AS
$$
DECLARE
    projection_table_name text;
    index_record          record;
BEGIN
    projection_table_name := 'p_' || name;
    FOR index_record IN
        SELECT indexname, indexdef
        FROM pg_indexes
        WHERE schemaname = schema
          AND tablename = projection_table_name
        LOOP
            INSERT INTO migration.pseudo_projection_indexes (schema_name, pseudo_projection_name, index_name, definition)
            VALUES (schema, name, index_record.indexname, index_record.indexdef)
            ON CONFLICT (schema_name, pseudo_projection_name, index_name) DO UPDATE
                SET definition = EXCLUDED.definition;
        END LOOP;
END
$$
    LANGUAGE plpgsql;


call drop_pseudo_projection('bi', 'project_contributions_data');

call create_pseudo_projection('bi', 'project_contributions_data', $$
SELECT p.id                                                             as project_id,
       count(distinct unnested.contributor_ids)                         as contributor_count,
       count(distinct cd.contribution_uuid) filter ( where cd.is_good_first_issue and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status != 'COMPLETED' and
                                                           cd.contribution_status !=
                                                           'CANCELLED') as good_first_issue_count,
       count(distinct cd.contribution_uuid) filter ( where cd.contribution_type = 'ISSUE' and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status != 'COMPLETED' and
                                                           cd.contribution_status !=
                                                           'CANCELLED') as available_issue_count,
       count(distinct cd.contribution_uuid) filter ( where gl.id is not null and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status != 'COMPLETED' and
                                                           cd.contribution_status !=
                                                           'CANCELLED') as live_hackathon_issue_count
FROM projects p
         LEFT JOIN bi.p_contribution_data cd ON cd.project_id = p.id
         LEFT JOIN bi.p_contribution_contributors_data ccd ON ccd.contribution_uuid = cd.contribution_uuid
         LEFT JOIN unnest(ccd.contributor_ids) unnested(contributor_ids) ON true
         LEFT JOIN hackathon_projects hp ON hp.project_id = p.id
         LEFT JOIN hackathons h ON h.id = hp.hackathon_id AND
                                   h.status = 'PUBLISHED' AND
                                   h.start_date <= now() AND
                                   h.end_date >= now()
         LEFT JOIN indexer_exp.github_labels gl ON gl.name = any (h.github_labels) AND gl.id = any (cd.github_label_ids)
GROUP BY p.id
$$, 'project_id');

call restore_pseudo_projection_indexes('bi', 'project_contributions_data');
