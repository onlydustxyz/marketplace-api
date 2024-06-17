alter table applications
    rename column applicant_id to old_applicant_id;

alter table applications
    add column applicant_id bigint references indexer_exp.github_accounts (id);

delete
from applications
where old_applicant_id not in
      (select u.id
       from iam.users u
                join indexer_exp.github_accounts ga on u.github_user_id = ga.id);

update applications
set applicant_id = u.github_user_id
from iam.all_users u
where u.user_id = old_applicant_id;

alter table applications
    drop column old_applicant_id;

alter table applications
    alter column applicant_id set not null;
