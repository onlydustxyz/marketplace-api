CREATE INDEX IF NOT EXISTS bi_p_per_contributor_contribution_data_cid_pid_ ON bi.p_per_contributor_contribution_data (contributor_id, project_id, timestamp desc);

CREATE TYPE contributor_activity_status as enum ('NEW', 'REACTIVATED', 'CHURNED', 'ACTIVE', 'INACTIVE');

DROP FUNCTION bi.select_contributors(fromDate timestamptz,
                                     toDate timestamptz,
                                     dataSourceIds uuid[],
                                     contributorIds bigint[],
                                     contributedTo uuid[],
                                     projectIds uuid[],
                                     projectSlugs text[],
                                     categoryIds uuid[],
                                     languageIds uuid[],
                                     ecosystemIds uuid[],
                                     countryCodes text[],
                                     contributionStatuses indexer_exp.contribution_status[],
                                     searchQuery text,
                                     filteredKpis boolean,
                                     includeApplicants boolean);
