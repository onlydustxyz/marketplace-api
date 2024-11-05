alter type contributor_activity_status rename to engagement_status;

create index on bi.p_contribution_data (project_id, contribution_status, timestamp);

DROP FUNCTION bi.select_projects(fromDate timestamptz,
                                 toDate timestamptz,
                                 dataSourceIds uuid[],
                                 programIds uuid[],
                                 projectIds uuid[],
                                 projectSlugs text[],
                                 projectLeadIds uuid[],
                                 categoryIds uuid[],
                                 languageIds uuid[],
                                 ecosystemIds uuid[],
                                 searchQuery text,
                                 showFilteredKpis boolean);