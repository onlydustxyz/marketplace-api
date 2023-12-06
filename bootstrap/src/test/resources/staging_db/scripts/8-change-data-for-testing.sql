update project_details
set visibility = 'PRIVATE'
where project_id = '27ca7e18-9e71-468f-8825-c64fe6b79d66'; -- B Conseil

insert into pending_project_leader_invitations (project_id, github_user_id)
values ('27ca7e18-9e71-468f-8825-c64fe6b79d66', 5160414);

update indexer_exp.github_app_installations
set suspended_at = now()
where id = 44300050; -- PierreOucif