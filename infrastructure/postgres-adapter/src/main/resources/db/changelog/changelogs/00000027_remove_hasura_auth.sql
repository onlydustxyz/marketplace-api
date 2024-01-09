drop trigger if exists insert_iam_user_from_auth_users_trigger on auth_users;
drop trigger if exists update_iam_user_email_from_auth_users_trigger on auth_users;
drop trigger if exists update_iam_user_from_auth_users_trigger on auth_users;
drop trigger if exists update_iam_user_roles_from_auth_users_trigger on auth_users;

drop trigger if exists insert_github_user_indexes_from_auth_users_trigger on auth.user_providers;
drop trigger if exists insert_user_indexing_jobs_from_auth_users_trigger on auth.user_providers;
drop trigger if exists replicate_user_providers_changes_trigger on auth.user_providers;

drop trigger if exists replicate_users_changes_trigger on auth.users;

drop function insert_iam_user_from_auth_users();
drop function update_iam_user_email_from_auth_users();
drop function update_iam_user_from_auth_users();
drop function update_iam_user_roles_from_auth_users();
drop function insert_github_user_indexes_from_auth_users();
drop function insert_user_indexing_jobs_from_auth_users();
drop function replicate_user_providers_changes();
drop function replicate_users_changes();
