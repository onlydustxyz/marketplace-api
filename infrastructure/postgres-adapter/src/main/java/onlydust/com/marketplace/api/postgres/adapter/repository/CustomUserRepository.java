package onlydust.com.marketplace.api.postgres.adapter.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserProfileEntity;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class CustomUserRepository {

    private static final String SELECT_USER_PROFILE_WHERE_ID = "select row_number() over (order by u.id) row_number," +
            "       u.id," +
            "       u.github_user_id," +
            "       u.email," +
            "       u.last_seen," +
            "       u.created_at," +
            "       gu.login," +
            "       gu.html_url," +
            "       coalesce(upi.bio, gu.bio) bio," +
            "       coalesce(upi.location, gu.location) location," +
            "       coalesce(upi.website, gu.website) website," +
            "       coalesce(upi.avatar_url, gu.avatar_url) avatar_url," +
            "       ci.public," +
            "       ci.channel," +
            "       ci.contact," +
            "       upi.languages," +
            "       upi.cover," +
            "       count.code_review_count," +
            "       count.issue_count," +
            "       count.pull_request_count," +
            "       count.week," +
            "       count.year," +
            "       (select count(pl.project_id)" +
            "        from project_leads pl" +
            "        where pl.user_id = ':userId') leading_project_number," +
            "       (select count(distinct project_id)" +
            "        from auth_users u" +
            "                 join contributions c on c.user_id = u.github_user_id" +
            "                 join project_github_repos gpr on gpr.github_repo_id = c.repo_id" +
            "        where u.id = ':userId'" +
            "        group by (gpr.project_id))                                 contributor_on_project," +
            "       (select sum(pr.amount)" +
            "        from auth_users u" +
            "                 join payment_requests pr on pr.recipient_id = u.github_user_id" +
            "        where u.id = ':userId')       total_earned," +
            "       (select count(distinct  c.id)" +
            "         from auth_users u" +
            "                  join contributions c on c.user_id = u.github_user_id" +
            "         where u.id = ':userId' and c.status = 'complete')" +
            " contributions_count" +
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

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static TypeReference<HashMap<String, Integer>> typeRef
            = new TypeReference<>() {
    };
    private final EntityManager entityManager;

    public UserProfile findProfileById(UUID userId) {
        final String query = SELECT_USER_PROFILE_WHERE_ID.replace(
                ":userId", userId.toString());
        LOGGER.debug(query);
        final List<UserProfileEntity> rows = entityManager.createNativeQuery(query, UserProfileEntity.class)
                .getResultList();
        final UserProfile userProfile = rowsToUserProfile(rows);
        return userProfile;
    }

    private static UserProfile rowsToUserProfile(List<UserProfileEntity> rows) {
        UserProfile userProfile = null;
        for (UserProfileEntity row : rows) {
            if (isNull(userProfile)) {
                final UserProfile.ProfileStats profileStats = UserProfile.ProfileStats.builder()
                        .totalEarned(row.getTotalEarned())
                        .leadedProjectCount(row.getNumberOfLeadingProject())
                        .contributedProjectCount(row.getNumberOfOwnContributorOnProject())
                        .contributionCount(row.getContributionsCount())
                        .build();
                userProfile = UserProfile.builder()
                        .id(row.getId())
                        .login(row.getLogin())
                        .bio(row.getBio())
                        .githubId(row.getGithubId())
                        .avatarUrl(row.getAvatarUrl())
                        .createAt(row.getCreatedAt())
                        .lastSeenAt(row.getLastSeen())
                        .htmlUrl(row.getHtmlUrl())
                        .location(row.getLocation())
                        .cover(isNull(row.getCover()) ? null : UserProfile.Cover.valueOf(row.getCover().toUpperCase()))
                        .website(row.getWebsite())
                        .technologies(getTechnologies(row))
                        .profileStats(profileStats)
                        .build();
            }
            if ((nonNull(row.getContact()) && !row.getContact().isEmpty()) && nonNull(row.getContactChannel()) && nonNull(row.getContactPublic())) {
                final UserProfile.ContactInformation contactInformation =
                        UserProfile.ContactInformation.builder()
                                .contact(row.getContact())
                                .channel(row.getContactChannel())
                                .visibility(row.getContactPublic() ? UserProfile.ContactInformation.Visibility.PUBLIC :
                                        UserProfile.ContactInformation.Visibility.PRIVATE)
                                .build();
                userProfile.addContactInformation(contactInformation);
            }
            if (nonNull(row.getYear()) && nonNull(row.getWeek())) {
                userProfile.getProfileStats().addContributionStat(UserProfile.ProfileStats.ContributionStats.builder()
                        .codeReviewCount(row.getCodeReviewCount())
                        .issueCount(row.getIssueCount())
                        .pullRequestCount(row.getPullRequestCount())
                        .week(row.getWeek())
                        .year(row.getYear())
                        .build());
            }
        }
        return userProfile;
    }

    private static HashMap<String, Integer> getTechnologies(UserProfileEntity row) {
        if (isNull(row.getLanguages())) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(row.getLanguages(), typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
