package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserProfileEntity;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
@Slf4j
public class CustomUserRepository {

    private static final String SELECT_USER_PROFILE_WHERE_ID = "select row_number() over (order by u.id) row_number," +
            "       u.id," +
            "       u.github_user_id," +
            "       u.email," +
            "       u.last_seen," +
            "       u.created_at," +
            "       u.admin," +
            "       gu.login," +
            "       gu.html_url," +
            "       coalesce(upi.bio, gu.bio) bio," +
            "       coalesce(upi.location, gu.location) location," +
            "       coalesce(upi.website, gu.website) website," +
            "       coalesce(upi.avatar_url, gu.avatar_url) avatar_url," +
            "       gu.twitter," +
            "       gu.linkedin," +
            "       gu.telegram," +
            "       ci.public," +
            "       ci.channel," +
            "       ci.contact," +
            "       upi.languages," +
            "       upi.weekly_allocated_time," +
            "       upi.looking_for_a_job," +
            "       upi.cover," +
            "       count.code_review_count," +
            "       count.issue_count," +
            "       count.pull_request_count," +
            "       count.week," +
            "       count.year" +
            " from public.auth_users u" +
            "         left join public.github_users gu on gu.id = u.github_user_id" +
            "         left join public.contact_informations ci on ci.user_id = u.id" +
            "         left join public.user_profile_info upi on upi.id = u.id" +
            "         left join (SELECT c.user_id                                                                    " +
            "          as github_user_id," +
            "                           date_part('year', c.created_at)                                        " +
            "          AS year," +
            "                           date_part('week', c.created_at)                                        " +
            "          AS week," +
            "                           count(DISTINCT c.details_id)" +
            "                           FILTER (WHERE c.type = 'issue')                           " +
            "          AS issue_count," +
            "                           count(DISTINCT c.details_id)" +
            "                           FILTER (WHERE c.type = 'code_review')                     " +
            "          AS code_review_count," +
            "                           count(DISTINCT c.details_id)" +
            "                           FILTER (WHERE c.type = 'pull_request')                    " +
            "          AS pull_request_count" +
            "                    FROM contributions c" +
            "                    where c.status = 'complete'" +
            "                    GROUP BY c.user_id, (date_part('year', c.created_at))," +
            "                             (date_part('week', c.created_at))) as count" +
            "                   on count.github_user_id = u.github_user_id" +
            " where u.id = ':userId'";

    private final EntityManager entityManager;

    public UserProfile findProfileById(UUID userId) {
        final String query = SELECT_USER_PROFILE_WHERE_ID.replace(
                ":userId", userId.toString());
        LOGGER.debug(query);
        final List<UserProfileEntity> rows = entityManager.createNativeQuery(query, UserProfileEntity.class)
                .getResultList();
        UserProfile userProfile = null;
        for (UserProfileEntity row : rows) {
            if (isNull(userProfile)) {
                userProfile = UserProfile.builder()
                        .id(row.getId())
                        .login(row.getLogin())
                        .bio(row.getBio())
                        .githubId(row.getGithubId())
                        .avatarUrl(row.getAvatarUrl())
                        .build();
            }
        }
        return userProfile;
    }
}
