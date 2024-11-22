create index if not exists p_contribution_data_issue_id_index
    on bi.p_contribution_data (issue_id);

create index if not exists p_contribution_data_pull_request_id_index
    on bi.p_contribution_data (pull_request_id);

create index if not exists p_contribution_data_code_review_id_index
    on bi.p_contribution_data (code_review_id);



-- call create_pseudo_projection('bi', 'contributor_application_data', $$...$$);
create or replace view bi.v_contributor_application_data as
SELECT v.*, md5(v::text) as hash
FROM (SELECT ga.id                                                            as contributor_id,
             array_agg(distinct ap.id) filter ( where ap.id is not null )     as applied_on_project_ids,
             array_agg(distinct ap.slug) filter ( where ap.slug is not null ) as applied_on_project_slugs
      FROM indexer_exp.github_accounts ga
               LEFT JOIN applications a on a.applicant_id = ga.id
               LEFT JOIN projects ap on ap.id = a.project_id
      GROUP BY ga.id) v;

