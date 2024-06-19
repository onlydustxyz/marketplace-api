alter table applications
    drop constraint applications_issue_id_comment_id_unique;

drop index applications_issue_id_comment_id_index;

alter table applications
    add constraint applications_project_id_applicant_id_issue_id_unique unique (project_id, applicant_id, issue_id);
