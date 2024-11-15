-- call create_pseudo_projection('bi', 'contribution_contributors_data', $$...$$);
create or replace view bi.v_contribution_contributors_data as
SELECT v.*, md5(v::text) as hash
FROM (select c.contribution_uuid                                                                    as contribution_uuid,
             c.repo_id                                                                              as repo_id,
             c.github_author_id                                                                     as github_author_id,
             array_agg(distinct gcc.contributor_id) filter ( where gcc.contributor_id is not null ) as contributor_ids,
             array_agg(distinct gia.user_id) filter ( where gia.user_id is not null )               as assignee_ids,
             array_agg(distinct a.applicant_id) filter ( where a.applicant_id is not null )         as applicant_ids,

             case when ad.contributor_id is not null then ad.contributor end                        as github_author,

             jsonb_agg(distinct jsonb_set(cd.contributor, '{since}', to_jsonb(gcc.tech_created_at::timestamptz), true))
             filter ( where cd.contributor_id is not null )                                         as contributors,

             jsonb_agg(distinct jsonb_set(jsonb_set(apd.contributor, '{since}', to_jsonb(a.received_at::timestamptz), true),
                                          '{applicationId}', to_jsonb(a.id), true))
             filter ( where apd.contributor_id is not null )                                        as applicants,

             concat(c.github_number, ' ',
                    c.github_title, ' ',
                    ad.contributor_login, ' ',
                    string_agg(distinct cd.contributor_login, ' '), ' ',
                    string_agg(distinct apd.contributor_login, ' ')
             )                                                                                      as search
      from indexer_exp.grouped_contributions c
               left join indexer_exp.grouped_contribution_contributors gcc on gcc.contribution_uuid = c.contribution_uuid
               left join bi.p_contributor_global_data cd on cd.contributor_id = gcc.contributor_id
               left join bi.p_contributor_global_data ad on ad.contributor_id = c.github_author_id
               left join indexer_exp.github_issues_assignees gia ON gia.issue_id = c.issue_id
               left join applications a on a.issue_id = c.issue_id
               left join bi.p_contributor_global_data apd on apd.contributor_id = a.applicant_id
      group by c.contribution_uuid, ad.contributor_id) v;

call refresh_pseudo_projection('bi', 'contribution_contributors_data', 'contribution_uuid');