package onlydust.com.marketplace.api.postgres.adapter.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectIdsForUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.RegisteredUserViewEntity;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class CustomUserRepository {

    private static final String SELECT_PROJECT_LEADERS = """
            select u.* from registered_users u join project_leads pl on pl.user_id = u.id and pl.project_id = :projectId
            """;

    private static final String SELECT_USER_PROFILE_WHERE_ID = """
            select row_number() over (order by u.id)                           row_number,
                   u.id,
                   u.github_user_id,
                   u.email,
                   u.last_seen,
                   u.created_at,
                   gu.login,
                   gu.html_url,
                   coalesce(upi.bio, gu.bio)                                   bio,
                   coalesce(upi.location, gu.location)                         location,
                   coalesce(upi.website, gu.website)                           website,
                   coalesce(upi.avatar_url, gu.avatar_url)                     avatar_url,
                   ci.public,
                   ci.channel,
                   ci.contact,
                   upi.languages,
                   upi.cover,
                   count.code_review_count,
                   count.issue_count,
                   count.pull_request_count,
                   count.week,
                   count.year,
                   (select count(pl.project_id)
                    from project_leads pl
                    where pl.user_id = :userId) leading_project_number,
                   (select count(distinct project_id)
                    from auth_users u
                             left join projects_contributors pc on pc.github_user_id = u.github_user_id
                    where u.id = :userId)                                 contributor_on_project,
                   (select sum(pr.amount)
                    from auth_users u
                             join payment_requests pr on pr.recipient_id = u.github_user_id
                    where u.id = :userId)       total_earned,
                   (select count(distinct c.id)
                    from auth_users u
                             join contributions c on c.user_id = u.github_user_id
                    where u.id = :userId
                      and c.status = 'complete')                               contributions_count
            from public.auth_users u
                     left join public.github_users gu on gu.id = u.github_user_id
                     left join public.contact_informations ci on ci.user_id = u.id
                     left join public.user_profile_info upi on upi.id = u.id
                     left join (SELECT c.user_id                                                           as github_user_id,
                                       date_part('year', c.created_at)                                     AS year,
                                       date_part('week', c.created_at)                                     AS week,
                                       count(DISTINCT c.details_id) FILTER (WHERE c.type = 'issue')        AS issue_count,
                                       count(DISTINCT c.details_id) FILTER (WHERE c.type = 'code_review')  AS code_review_count,
                                       count(DISTINCT c.details_id) FILTER (WHERE c.type = 'pull_request') AS pull_request_count
                                FROM contributions c
                                where c.status = 'complete'
                                GROUP BY c.user_id, (date_part('year', c.created_at)), (date_part('week', c.created_at))) as count
                               on count.github_user_id = u.github_user_id
            where u.id = :userId
            """;

    private final static String GET_PROJECT_STATS_BY_USER = """
            select p.project_id as                       project_id,
                   (select count(distinct github_user_id)
                    from projects_contributors
                    where project_id = p.project_id)     contributors_count,
                   (select distinct b.initial_amount - b.remaining_amount total_usd_granted
                    from project_details pd
                             left join project_leads pl on pl.project_id = pd.project_id
                             left join projects_budgets pb on pb.project_id = pd.project_id
                             left join budgets b on b.id = pb.budget_id
                    where pd.project_id = p.project_id
                      and b.currency = 'usd')            total_granted,
                   (select count(distinct c.id)
                    from project_github_repos pgr
                             left join contributions c
                                       on c.repo_id = pgr.github_repo_id and c.status = 'complete'
                             left join auth_users au on au.github_user_id = c.user_id and au.id = :userId
                    where pgr.project_id = p.project_id) user_contributions_count,
                   (select c.closed_at
                    from project_github_repos pgr
                             left join contributions c
                                       on c.repo_id = pgr.github_repo_id and c.status = 'complete'
                            left join auth_users au on au.github_user_id = c.user_id and au.id = :userId
                    where pgr.project_id = p.project_id
                      and closed_at is not null
                    order by c.closed_at desc
                    limit 1)                             last_contribution_date,
                   p.is_lead,
                   p.name,
                   p.logo_url
            from ((select distinct pd.project_id, false is_lead, pd.name, pd.logo_url
                   from auth_users u
                            join contributions c on c.user_id = u.github_user_id
                            join project_github_repos gpr on gpr.github_repo_id = c.repo_id
                            join project_details pd on pd.project_id = gpr.project_id
                   where u.id = :userId and c.status = 'complete')
                  UNION
                  (select distinct pd.project_id, true is_lead, pd.name, pd.logo_url
                   from auth_users u
                            left join project_leads pl on pl.user_id = u.id
                            left join project_details pd on pd.project_id = pl.project_id
                   where u.id = :userId)) as p
            order by p.is_lead desc""";
    private final static TypeReference<HashMap<String, Integer>> typeRef
            = new TypeReference<>() {
    };
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EntityManager entityManager;

    private UserProfileView rowsToUserProfile(List<UserProfileEntity> rows) {
        UserProfileView userProfileView = null;
        for (UserProfileEntity row : rows) {
            if (isNull(userProfileView)) {
                final UserProfileView.ProfileStats profileStats = UserProfileView.ProfileStats.builder()
                        .totalEarned(row.getTotalEarned())
                        .leadedProjectCount(row.getNumberOfLeadingProject())
                        .contributedProjectCount(row.getNumberOfOwnContributorOnProject())
                        .contributionCount(row.getContributionsCount())
                        .build();
                userProfileView = UserProfileView.builder()
                        .id(row.getId())
                        .login(row.getLogin())
                        .bio(row.getBio())
                        .githubId(row.getGithubId())
                        .avatarUrl(row.getAvatarUrl())
                        .createAt(row.getCreatedAt())
                        .lastSeenAt(row.getLastSeen())
                        .htmlUrl(row.getHtmlUrl())
                        .location(row.getLocation())
                        .cover(isNull(row.getCover()) ? null :
                                UserProfileView.Cover.valueOf(row.getCover().toUpperCase()))
                        .website(row.getWebsite())
                        .technologies(getTechnologies(row))
                        .profileStats(profileStats)
                        .build();
            }
            if ((nonNull(row.getContact()) && !row.getContact().isEmpty()) && nonNull(row.getContactChannel()) && nonNull(row.getContactPublic())) {
                final UserProfileView.ContactInformation contactInformation =
                        UserProfileView.ContactInformation.builder()
                                .contact(row.getContact())
                                .channel(row.getContactChannel())
                                .visibility(row.getContactPublic() ?
                                        UserProfileView.ContactInformation.Visibility.PUBLIC :
                                        UserProfileView.ContactInformation.Visibility.PRIVATE)
                                .build();
                userProfileView.addContactInformation(contactInformation);
            }
            if (nonNull(row.getYear()) && nonNull(row.getWeek())) {
                userProfileView.getProfileStats().addContributionStat(UserProfileView.ProfileStats.ContributionStats.builder()
                        .codeReviewCount(row.getCodeReviewCount())
                        .issueCount(row.getIssueCount())
                        .pullRequestCount(row.getPullRequestCount())
                        .week(row.getWeek())
                        .year(row.getYear())
                        .build());
            }
        }
        return userProfileView;
    }

    private HashMap<String, Integer> getTechnologies(UserProfileEntity row) {
        if (isNull(row.getLanguages())) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(row.getLanguages(), typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RegisteredUserViewEntity> findProjectLeaders(UUID projectId) {
        return entityManager
                .createNativeQuery(SELECT_PROJECT_LEADERS, RegisteredUserViewEntity.class)
                .setParameter("projectId", projectId)
                .getResultList();
    }

    public Optional<UserProfileView> findProfileById(UUID userId) {
        final List<UserProfileEntity> rows = entityManager.createNativeQuery(SELECT_USER_PROFILE_WHERE_ID,
                        UserProfileEntity.class)
                .setParameter("userId", userId)
                .getResultList();
        return Optional.ofNullable(rowsToUserProfile(rows));
    }

    public List<ProjectIdsForUserEntity> getProjectIdsForUserId(final UUID userId) {
        return entityManager.createNativeQuery(GET_PROJECT_STATS_BY_USER, ProjectIdsForUserEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public UserPayoutInfoEntity getUserPayoutInfoById(UUID id) {
        final List resultList = entityManager.createNativeQuery("select * from user_payout_info limit 1", UserPayoutInfoEntity.class)
                .getResultList();
        return (UserPayoutInfoEntity) resultList.get(0);
    }
}
