CREATE OR REPLACE PROCEDURE save_pseudo_projection_indexes_definition(schema text, name text)
AS
$$
DECLARE
    projection_table_name text;
    index_record          record;
BEGIN
    DELETE FROM migration.pseudo_projection_indexes WHERE schema_name = schema AND pseudo_projection_name = name;
    
    projection_table_name := 'p_' || name;
    FOR index_record IN
        SELECT indexname, indexdef
        FROM pg_indexes
        WHERE schemaname = schema
          AND tablename = projection_table_name
        LOOP
            INSERT INTO migration.pseudo_projection_indexes (schema_name, pseudo_projection_name, index_name, definition)
            VALUES (schema, name, index_record.indexname, index_record.indexdef);
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
                                                           cd.contribution_status = 'IN_PROGRESS') as good_first_issue_count,

       count(distinct cd.contribution_uuid) filter ( where cd.contribution_type = 'ISSUE' and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status = 'IN_PROGRESS' and
                                                           NOT EXISTS (
                                                               SELECT 1 
                                                               FROM hackathons h
                                                               JOIN hackathon_projects hp ON h.id = hp.hackathon_id
                                                               JOIN indexer_exp.github_labels gl ON gl.name = any (h.github_labels)
                                                               WHERE hp.project_id = p.id
                                                                 AND h.status = 'PUBLISHED'
                                                                 AND h.start_date > now()
                                                                 AND gl.id = any (cd.github_label_ids)
                                                           )) as available_issue_count,

       count(distinct cd.contribution_uuid) filter ( where cd.contribution_type = 'ISSUE' and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status = 'IN_PROGRESS' and
                                                           EXISTS (
                                                               SELECT 1 
                                                               FROM hackathons h
                                                               JOIN hackathon_projects hp ON h.id = hp.hackathon_id
                                                               JOIN indexer_exp.github_labels gl ON gl.name = any (h.github_labels)
                                                               WHERE hp.project_id = p.id
                                                                 AND h.status = 'PUBLISHED'
                                                                 AND h.start_date <= now()
                                                                 AND h.end_date >= now()
                                                                 AND gl.id = any (cd.github_label_ids)
                                                           )) as live_hackathon_issue_count,

       count(distinct cd.contribution_uuid) filter ( where cd.contribution_type = 'PULL_REQUEST' and
                                                           cd.contribution_status = 'COMPLETED') as merged_pr_count

FROM projects p
         LEFT JOIN bi.p_contribution_data cd ON cd.project_id = p.id
         LEFT JOIN bi.p_contribution_contributors_data ccd ON ccd.contribution_uuid = cd.contribution_uuid
         LEFT JOIN unnest(ccd.contributor_ids) unnested(contributor_ids) ON true
GROUP BY p.id
$$, 'project_id');

call restore_pseudo_projection_indexes('bi', 'project_contributions_data');
