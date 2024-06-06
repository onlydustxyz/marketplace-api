create view public_projects as
select id,
       slug,
       rank,
       name,
       logo_url,
       short_description,
       long_description,
       hiring,
       reward_ignore_pull_requests_by_default,
       reward_ignore_issues_by_default,
       reward_ignore_code_reviews_by_default,
       reward_ignore_contributions_before_date_by_default
from projects
where visibility = 'PUBLIC';
