call drop_pseudo_projection('bi', 'project_contributions_data');

call create_pseudo_projection('bi', 'project_contributions_data', $$
SELECT p.id                                                                                       as project_id,
       count(distinct unnested.contributor_ids)                                                   as contributor_count,
       count(distinct cd.contribution_uuid) filter ( where cd.is_good_first_issue and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status != 'COMPLETED' and
                                                           cd.contribution_status != 'CANCELLED') as good_first_issue_count,
       count(distinct cd.contribution_uuid) filter ( where gl.id is not null and
                                                           coalesce(array_length(ccd.assignee_ids, 1), 0) = 0 and
                                                           cd.contribution_status != 'COMPLETED' and
                                                           cd.contribution_status != 'CANCELLED') as live_hackathon_issue_count
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

create unique index on bi.p_project_contributions_data (project_id, contributor_count, good_first_issue_count, live_hackathon_issue_count);
