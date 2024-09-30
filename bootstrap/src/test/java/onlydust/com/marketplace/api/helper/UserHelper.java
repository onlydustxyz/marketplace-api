package onlydust.com.marketplace.api.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserHelper {
    @Autowired
    private DatabaseHelper databaseHelper;

    public void setPreferredLanguages(UserAuthHelper.AuthenticatedUser user, String... languageSlugs) {
        databaseHelper.executeQuery("""
                with new_languages as (
                    select array_agg(id) ids
                    from languages
                    where slug = any(:languageSlugs)
                )
                insert into user_profile_info(id, preferred_language_ids)
                select :userId, l.ids
                from new_languages l
                on conflict(id) do update
                set preferred_language_ids = excluded.preferred_language_ids
                """, Map.of(
                "userId", user.userId().value(),
                "languageSlugs", languageSlugs
        ));
    }
}
