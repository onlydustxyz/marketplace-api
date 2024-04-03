update accounting.billing_profiles_users
set joined_at = bp.tech_created_at
from accounting.billing_profiles bp
where bp.id = billing_profiles_users.billing_profile_id;
