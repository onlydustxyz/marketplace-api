package onlydust.com.marketplace.api.postgres.adapter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectStatsForUserQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserProfileQueryEntity;
import onlydust.com.marketplace.project.domain.model.Contact;
import onlydust.com.marketplace.project.domain.model.UserAllocatedTimeToContribute;
import onlydust.com.marketplace.project.domain.view.TotalsEarned;
import onlydust.com.marketplace.project.domain.view.UserProfileView;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@AllArgsConstructor
@Slf4j
public class CustomUserRepository {

    private static final String SELECT_USER_PROFILE = """
            select u.github_user_id,
                   u.id,
                   u.email,
                   u.last_seen_at,
                   u.created_at,
                   coalesce(gu.login, u.github_login)      login,
                   gu.html_url,
                   coalesce(upi.bio, gu.bio)               bio,
                   coalesce(upi.location, gu.location)     location,
                   coalesce(upi.website, gu.website)       website,
                   coalesce(upi.avatar_url, gu.avatar_url, u.github_avatar_url) avatar_url,
                   upi.languages,
                   upi.cover,
                   upi.looking_for_a_job,
                   upi.weekly_allocated_time,
                   upi.first_name,
                   upi.last_name,
                   (SELECT jsonb_agg(jsonb_build_object(
                           'is_public', ci.public,
                           'channel', ci.channel,
                           'contact', ci.contact
                                     ))
                    FROM public.contact_informations ci
                    WHERE u.id is not null
                      and ci.user_id = u.id)               contacts,
                        
                   (SELECT jsonb_agg(jsonb_build_object(
                           'year', cc.year,
                           'week', cc.week,
                           'issue_count', cc.issue_count,
                           'code_review_count', cc.code_review_count,
                           'pull_request_count', cc.pull_request_count
                                     ))
                    FROM (SELECT date_part('isoyear', c.created_at)                                         AS year,
                                 date_part('week', c.created_at)                                            AS week,
                                 count(DISTINCT c.issue_id) FILTER (WHERE c.type = 'ISSUE')                 AS issue_count,
                                 count(DISTINCT c.code_review_id) FILTER (WHERE c.type = 'CODE_REVIEW')     AS code_review_count,
                                 count(DISTINCT c.pull_request_id) FILTER (WHERE c.type = 'PULL_REQUEST')   AS pull_request_count
                          FROM indexer_exp.contributions c
                          where c.status = 'COMPLETED'
                            and c.contributor_id = u.github_user_id
                          GROUP BY year, week) as cc)      counts,
                        
                   (select count(pl.project_id)
                    from project_leads pl
                    where u.id is not null
                      and pl.user_id = u.id)               leading_project_number,
                        
                   (select count(distinct pc.project_id)
                    from projects_contributors pc
                    where pc.github_user_id = u.github_user_id)       contributor_on_project,
                        
                   (select jsonb_agg(jsonb_build_object(
                                   'total_amount', user_rewards.total_amount,
                                   'total_dollars_equivalent', user_rewards.total_dollars_equivalent,
                                   'currency_id', user_rewards.currency_id,
                                   'currency_code', user_rewards.currency_code,
                                   'currency_name', user_rewards.currency_name,
                                   'currency_decimals', user_rewards.currency_decimals,
                                   'currency_latest_usd_quote', user_rewards.currency_latest_usd_quote,
                                   'currency_logo_url', user_rewards.currency_logo_url
                                ))
                    from (select sum(r.amount)  as total_amount,
                                 coalesce(sum(rsd.amount_usd_equivalent), 0)  as total_dollars_equivalent,
                                 c.id as currency_id,
                                 c.code as currency_code,
                                 c.name as currency_name,
                                 c.decimals as currency_decimals,
                                 luq.price as currency_latest_usd_quote,
                                 c.logo_url as currency_logo_url
                          from rewards r
                          join accounting.reward_status_data rsd on rsd.reward_id = r.id
                          join currencies c on c.id = r.currency_id
                          left join accounting.latest_usd_quotes luq on luq.currency_id = c.id
                          where r.recipient_id = u.github_user_id
                          group by c.id, luq.price) as user_rewards)    totals_earned,
                        
                   (select sum(rc.completed_contribution_count)
                    from indexer_exp.repos_contributors rc
                    join indexer_exp.github_repos gr on gr.id = rc.repo_id and gr.visibility = 'PUBLIC'
                    where rc.contributor_id = u.github_user_id)           contributions_count
                
            """;

    private final static String SELECT_USER_PROFILE_WHERE_ID = SELECT_USER_PROFILE + """
            from iam.users u
                     left join indexer_exp.github_accounts gu on gu.id = u.github_user_id
                     left join public.user_profile_info upi on upi.id = u.id
            where u.id = :userId
            """;

    private final static String GET_PROJECT_STATS_BY_USER = """
            with granted_usd as (
              select 
                pa.project_id,
                SUM(pa.initial_allowance * COALESCE(luq.price, 0) - pa.current_allowance * COALESCE(luq.price, 0)) total
              from project_allowances pa
                left join accounting.latest_usd_quotes luq on luq.currency_id = pa.currency_id
              group by pa.project_id
            )
            select  p.id,
                    p.slug,
                    p.is_lead,
                    p.assigned_at as lead_since,
                    p.name,
                    p.logo_url,
                    p.visibility,
                        
                   (select count(distinct github_user_id)
                    from projects_contributors
                    where project_id = p.id)     contributors_count,
                    
                   granted_usd.total as total_granted,
                      
                   (select sum(rc.completed_contribution_count)
                    from project_github_repos pgr
                    join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id and gr.visibility = 'PUBLIC'
                    join indexer_exp.repos_contributors rc on rc.repo_id = gr.id and rc.contributor_id = :githubUserId
                    where pgr.project_id = p.id and gr.visibility = 'PUBLIC') user_contributions_count,
                    
                   (select max(c.completed_at)
                    from project_github_repos pgr
                    join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                    join indexer_exp.contributions c on c.repo_id = gr.id and c.status = 'COMPLETED' and c.completed_at is not null and c.contributor_id = :githubUserId            
                    where pgr.project_id = p.id and gr.visibility = 'PUBLIC') last_contribution_date,
                    
                    
                   (select min(c.completed_at)
                    from project_github_repos pgr
                    join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                    join indexer_exp.contributions c on c.repo_id = pgr.github_repo_id and c.status = 'COMPLETED' and c.completed_at is not null and c.contributor_id = :githubUserId
                    where pgr.project_id = p.id and gr.visibility = 'PUBLIC') first_contribution_date
                   
            from ((select distinct p.id, false is_lead, cast(null as timestamp) as assigned_at, p.name, p.logo_url, p.slug, p.visibility
                   from indexer_exp.repos_contributors rc
                            join indexer_exp.github_repos gr on gr.id = rc.repo_id
                            join project_github_repos gpr on gpr.github_repo_id = gr.id
                            join projects p on p.id = gpr.project_id
                   where rc.contributor_id = :githubUserId and rc.completed_contribution_count > 0 and gr.visibility = 'PUBLIC')
                  UNION
                  (select distinct p.id, true is_lead, pl.assigned_at, p.name, p.logo_url, p.slug, p.visibility
                   from iam.users u
                            join project_leads pl on pl.user_id = u.id
                            join projects p on p.id = pl.project_id
                   where u.github_user_id = :githubUserId)) as p
            left join granted_usd on granted_usd.project_id = p.id
            order by p.is_lead desc""";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EntityManager entityManager;

    private UserProfileView rowToUserProfile(UserProfileQueryEntity row) {
        return UserProfileView.builder()
                .id(row.id())
                .login(row.login())
                .bio(row.bio())
                .githubId(row.githubId())
                .avatarUrl(row.avatarUrl())
                .createAt(row.createdAt())
                .lastSeenAt(row.lastSeenAt())
                .htmlUrl(row.htmlUrl())
                .location(row.location())
                .website(row.website())
                .profileStats(UserProfileView.ProfileStats.builder()
                        .totalsEarned(new TotalsEarned(isNull(row.totalEarnedPerCurrencies())
                                ? List.of()
                                : row.totalEarnedPerCurrencies().stream().map(UserProfileQueryEntity.TotalEarnedPerCurrency::toDomain).toList())
                        )
                        .leadedProjectCount(row.numberOfLeadingProject())
                        .contributedProjectCount(row.numberOfOwnContributorOnProject())
                        .contributionCount(row.contributionsCount())
                        .contributionStats(isNull(row.counts()) ? List.of() :
                                row.counts().stream().map(UserProfileQueryEntity.WeekCount::toDomain)
                                        .sorted(new UserProfileView.ProfileStats.ContributionStatsComparator())
                                        .collect(Collectors.toList())
                        )
                        .build())
                .isLookingForAJob(row.isLookingForAJob())
                .allocatedTimeToContribute(isNull(row.allocatedTimeToContribute()) ? null :
                        switch (row.allocatedTimeToContribute()) {
                            case none -> UserAllocatedTimeToContribute.NONE;
                            case less_than_one_day -> UserAllocatedTimeToContribute.LESS_THAN_ONE_DAY;
                            case one_to_three_days -> UserAllocatedTimeToContribute.ONE_TO_THREE_DAYS;
                            case greater_than_three_days -> UserAllocatedTimeToContribute.GREATER_THAN_THREE_DAYS;
                        })
                .contacts(getContacts(row))
                .firstName(row.firstName())
                .lastName(row.lastName())
                .build();
    }

    private Set<Contact> getContacts(UserProfileQueryEntity row) {
        final List<UserProfileQueryEntity.Contact> contactEntities = row.contacts() != null ? row.contacts() : List.of();

        final var contacts = contactEntities.stream().map(contact -> Contact.builder()
                .channel(isNull(contact.channel()) ? null : switch (contact.channel()) {
                    case TELEGRAM -> Contact.Channel.TELEGRAM;
                    case TWITTER -> Contact.Channel.TWITTER;
                    case DISCORD -> Contact.Channel.DISCORD;
                    case LINKEDIN -> Contact.Channel.LINKEDIN;
                    case WHATSAPP -> Contact.Channel.WHATSAPP;
                })
                .visibility(Boolean.TRUE.equals(contact.isPublic()) ? Contact.Visibility.PUBLIC :
                        Contact.Visibility.PRIVATE)
                .build()
        ).collect(Collectors.toMap(Contact::getChannel, contact -> contact, (a, b) -> a));

        return new HashSet<>(contacts.values());
    }

    public Optional<UserProfileView> findProfileById(final UUID userId) {
        try {
            final UserProfileQueryEntity row =
                    (UserProfileQueryEntity) entityManager.createNativeQuery(SELECT_USER_PROFILE_WHERE_ID,
                                    UserProfileQueryEntity.class)
                            .setParameter("userId", userId)
                            .getSingleResult();
            return Optional.ofNullable(rowToUserProfile(row));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<ProjectStatsForUserQueryEntity> getProjectsStatsForUser(final Long githubUserId) {
        return entityManager.createNativeQuery(GET_PROJECT_STATS_BY_USER, ProjectStatsForUserQueryEntity.class)
                .setParameter("githubUserId", githubUserId)
                .getResultList();
    }
}
