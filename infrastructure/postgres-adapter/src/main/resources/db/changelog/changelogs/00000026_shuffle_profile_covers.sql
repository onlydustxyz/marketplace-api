with changes as (select id,
                        ('x' || translate(id::text, '-', ''))::bit(32)::bigint % 4 as new_cover_index
                 from user_profile_info)
update user_profile_info
set cover = case
                when new_cover_index = 0 then 'cyan'::profile_cover
                when new_cover_index = 1 then 'magenta'::profile_cover
                when new_cover_index = 2 then 'yellow'::profile_cover
                when new_cover_index = 3 then 'blue'::profile_cover
    end
from changes
where user_profile_info.id = changes.id;

