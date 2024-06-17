alter table applications
    add column issue_id                 bigint references indexer_exp.github_issues (id),
    add column comment_id               bigint,
    add column motivations              text,
    add column problem_solving_approach text,
    add column tech_created_at          timestamp not null default now(),
    add column tech_updated_at          timestamp not null default now(),
    add constraint applications_issue_id_comment_id_unique unique (issue_id, comment_id),
    add constraint applications_project_id_fkey foreign key (project_id) references projects (id),
    add constraint applications_applicant_id_fkey foreign key (applicant_id) references iam.users (id)
;

create index applications_issue_id_comment_id_index on applications (issue_id, comment_id);

create trigger applications_set_tech_updated_at
    before update
    on applications
    for each row
execute function set_tech_updated_at();
