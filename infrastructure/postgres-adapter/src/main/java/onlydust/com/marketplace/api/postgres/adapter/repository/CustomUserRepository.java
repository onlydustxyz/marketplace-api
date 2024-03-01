package onlydust.com.marketplace.api.postgres.adapter.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectStatsForUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserProfileEntity;
import onlydust.com.marketplace.project.domain.model.Contact;
import onlydust.com.marketplace.project.domain.model.UserAllocatedTimeToContribute;
import onlydust.com.marketplace.project.domain.view.TotalEarnedPerCurrency;
import onlydust.com.marketplace.project.domain.view.TotalsEarned;
import onlydust.com.marketplace.project.domain.view.UserProfileView;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContactChanelEnumEntity.email;

@AllArgsConstructor
@Slf4j
public class CustomUserRepository {

    private static final String SELECT_USER_PROFILE = """
            select gu.id as                                github_user_id,
                   u.id,
                   u.email as email,
                   u.last_seen_at,
                   u.created_at,
                   gu.login,
                   gu.html_url,
                   coalesce(upi.bio, gu.bio)               bio,
                   coalesce(upi.location, gu.location)     location,
                   coalesce(upi.website, gu.website)       website,
                   coalesce(upi.avatar_url, gu.avatar_url) avatar_url,
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
                            and c.contributor_id = gu.id
                          GROUP BY year, week) as cc)      counts,
                        
                   (select count(pl.project_id)
                    from project_leads pl
                    where u.id is not null
                      and pl.user_id = u.id)               leading_project_number,
                        
                   (select count(distinct pc.project_id)
                    from projects_contributors pc
                    where pc.github_user_id = gu.id)       contributor_on_project,
                        
                   (select jsonb_build_object(
                                   'total_dollars_equivalent', sum(prs.total_dollars_equivalent),
                                   'details',
                                   jsonb_agg(jsonb_build_object(
                                           'total_amount', prs.total_amount,
                                           'total_dollars_equivalent', prs.total_dollars_equivalent,
                                           'currency', prs.currency
                                             )))
                    from (select sum(r.amount)  as total_amount,
                                 coalesce(sum(rsd.amount_usd_equivalent), 0)  as total_dollars_equivalent,
                                 LOWER(c.code)  as currency
                          from rewards r
                          join accounting.reward_status_data rsd on rsd.reward_id = r.id
                          join currencies c on c.id = r.currency_id
                          where r.recipient_id = gu.id
                          group by c.code) as prs)    totals_earned,
                        
                   (select sum(rc.completed_contribution_count)
                    from indexer_exp.repos_contributors rc
                    join indexer_exp.github_repos gr on gr.id = rc.repo_id and gr.visibility = 'PUBLIC'
                    where rc.contributor_id = gu.id)           contributions_count
                
            """;

    private final static String SELECT_USER_PROFILE_WHERE_ID = SELECT_USER_PROFILE + """
            from iam.users u
                     join indexer_exp.github_accounts gu on gu.id = u.github_user_id
                     left join public.user_profile_info upi on upi.id = u.id
            where u.id = :userId
            """;

    private final static String SELECT_USER_PROFILE_WHERE_GITHUB_ID = SELECT_USER_PROFILE + """
            from indexer_exp.github_accounts gu
                     left join iam.users u on gu.id = u.github_user_id
                     left join public.user_profile_info upi on upi.id = u.id
            where gu.id = :githubUserId
            """;

    private final static String SELECT_USER_PROFILE_WHERE_GITHUB_LOGIN = SELECT_USER_PROFILE + """
            from indexer_exp.github_accounts gu
                     left join iam.users u on gu.id = u.github_user_id
                     left join public.user_profile_info upi on upi.id = u.id
            where gu.login = :githubLogin
            """;

    private final static String GET_PROJECT_STATS_BY_USER = """
            with granted_usd as (
              select 
                pb.project_id,
                SUM(b.initial_amount * COALESCE(CASE WHEN b.currency = 'usd' then 1 ELSE cuq.price END, 0) -
                     b.remaining_amount * COALESCE(CASE WHEN b.currency = 'usd' then 1 ELSE cuq.price END, 0)) total
              from projects_budgets pb
                join budgets b on b.id = pb.budget_id
                left join crypto_usd_quotes cuq on cuq.currency = b.currency
              group by pb.project_id
            )
            select  p.project_id,
                    p.key as slug,
                    p.is_lead,
                    p.assigned_at as lead_since,
                    p.name,
                    p.logo_url,
                    p.visibility,
                        
                   (select count(distinct github_user_id)
                    from projects_contributors
                    where project_id = p.project_id)     contributors_count,
                    
                   granted_usd.total as total_granted,
                      
                   (select sum(rc.completed_contribution_count)
                    from project_github_repos pgr
                    join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id and gr.visibility = 'PUBLIC'
                    join indexer_exp.repos_contributors rc on rc.repo_id = gr.id and rc.contributor_id = :githubUserId
                    where pgr.project_id = p.project_id and gr.visibility = 'PUBLIC') user_contributions_count,
                    
                   (select max(c.completed_at)
                    from project_github_repos pgr
                    join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                    join indexer_exp.contributions c on c.repo_id = gr.id and c.status = 'COMPLETED' and c.completed_at is not null and c.contributor_id = :githubUserId            
                    where pgr.project_id = p.project_id and gr.visibility = 'PUBLIC') last_contribution_date,
                    
                    
                   (select min(c.completed_at)
                    from project_github_repos pgr
                    join indexer_exp.github_repos gr on gr.id = pgr.github_repo_id
                    join indexer_exp.contributions c on c.repo_id = pgr.github_repo_id and c.status = 'COMPLETED' and c.completed_at is not null and c.contributor_id = :githubUserId
                    where pgr.project_id = p.project_id and gr.visibility = 'PUBLIC') first_contribution_date
                   
            from ((select distinct pd.project_id, false is_lead, cast(null as timestamp) as assigned_at, pd.name, pd.logo_url, pd.key, pd.visibility
                   from indexer_exp.repos_contributors rc
                            join indexer_exp.github_repos gr on gr.id = rc.repo_id
                            join project_github_repos gpr on gpr.github_repo_id = gr.id
                            join project_details pd on pd.project_id = gpr.project_id
                   where rc.contributor_id = :githubUserId and rc.completed_contribution_count > 0 and gr.visibility = 'PUBLIC')
                  UNION
                  (select distinct pd.project_id, true is_lead, pl.assigned_at, pd.name, pd.logo_url, pd.key, pd.visibility
                   from iam.users u
                            join project_leads pl on pl.user_id = u.id
                            join project_details pd on pd.project_id = pl.project_id
                   where u.github_user_id = :githubUserId)) as p
            left join granted_usd on granted_usd.project_id = p.project_id
            order by p.is_lead desc""";
    private final static TypeReference<HashMap<String, Long>> typeRef
            = new TypeReference<>() {
    };
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EntityManager entityManager;

    private UserProfileView rowToUserProfile(UserProfileEntity row) {
        return UserProfileView.builder()
                .id(row.getId())
                .login(row.getLogin())
                .bio(row.getBio())
                .githubId(row.getGithubId())
                .avatarUrl(row.getAvatarUrl())
                .createAt(row.getCreatedAt())
                .lastSeenAt(row.getLastSeenAt())
                .htmlUrl(row.getHtmlUrl())
                .location(row.getLocation())
                .cover(isNull(row.getCover()) ? null : row.getCover().toDomain())
                .website(row.getWebsite())
                .technologies(getTechnologies(row))
                .profileStats(UserProfileView.ProfileStats.builder()
                        .totalsEarned(isNull(row.getTotalsEarned()) ? null :
                                TotalsEarned.builder()
                                        .totalDollarsEquivalent(row.getTotalsEarned().getTotalDollarsEquivalent())
                                        .details(isNull(row.getTotalsEarned().getDetails()) ? List.of() :
                                                row.getTotalsEarned().getDetails().stream().map(detail ->
                                                        TotalEarnedPerCurrency.builder()
                                                                .currency(isNull(detail.getCurrency()) ? null :
                                                                        detail.getCurrency().toDomain())
                                                                .totalAmount(detail.getTotalAmount())
                                                                .totalDollarsEquivalent(detail.getTotalDollarsEquivalent())
                                                                .build()
                                                ).collect(Collectors.toList()))
                                        .build())
                        .leadedProjectCount(row.getNumberOfLeadingProject())
                        .contributedProjectCount(row.getNumberOfOwnContributorOnProject())
                        .contributionCount(row.getContributionsCount())
                        .contributionStats(isNull(row.getCounts()) ? List.of() :
                                row.getCounts().stream().map(UserProfileEntity.WeekCount::toDomain)
                                        .sorted(new UserProfileView.ProfileStats.ContributionStatsComparator())
                                        .collect(Collectors.toList())
                        )
                        .build())
                .isLookingForAJob(row.getIsLookingForAJob())
                .allocatedTimeToContribute(isNull(row.getAllocatedTimeToContribute()) ? null :
                        switch (row.getAllocatedTimeToContribute()) {
                            case none -> UserAllocatedTimeToContribute.NONE;
                            case less_than_one_day -> UserAllocatedTimeToContribute.LESS_THAN_ONE_DAY;
                            case one_to_three_days -> UserAllocatedTimeToContribute.ONE_TO_THREE_DAYS;
                            case greater_than_three_days -> UserAllocatedTimeToContribute.GREATER_THAN_THREE_DAYS;
                        })
                .contacts(getContacts(row))
                .firstName(row.getFirstName())
                .lastName(row.getLastName())
                .build();
    }

    private Set<Contact> getContacts(UserProfileEntity row) {
        final List<UserProfileEntity.Contact> contactEntities = row.getContacts() != null ? row.getContacts() : List.of();

        final var contacts = contactEntities.stream().map(contact -> Contact.builder()
                .contact(email.equals(contact.getChannel()) ? row.getEmail() : contact.getContact())
                .channel(isNull(contact.getChannel()) ? null : switch (contact.getChannel()) {
                    case email -> Contact.Channel.EMAIL;
                    case telegram -> Contact.Channel.TELEGRAM;
                    case twitter -> Contact.Channel.TWITTER;
                    case discord -> Contact.Channel.DISCORD;
                    case linkedin -> Contact.Channel.LINKEDIN;
                    case whatsapp -> Contact.Channel.WHATSAPP;
                })
                .visibility(Boolean.TRUE.equals(contact.getIsPublic()) ? Contact.Visibility.PUBLIC :
                        Contact.Visibility.PRIVATE)
                .build()
        ).collect(Collectors.toMap(Contact::getChannel, contact -> contact, (a, b) -> a));

        contacts.putIfAbsent(Contact.Channel.EMAIL, Contact.builder()
                .contact(row.getEmail())
                .channel(Contact.Channel.EMAIL)
                .visibility(Contact.Visibility.PRIVATE)
                .build());

        return new HashSet<>(contacts.values());
    }

    private HashMap<String, Long> getTechnologies(UserProfileEntity row) {
        if (isNull(row.getLanguages())) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(row.getLanguages(), typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<UserProfileView> findProfileById(final UUID userId) {
        try {
            final UserProfileEntity row =
                    (UserProfileEntity) entityManager.createNativeQuery(SELECT_USER_PROFILE_WHERE_ID,
                                    UserProfileEntity.class)
                            .setParameter("userId", userId)
                            .getSingleResult();
            return Optional.ofNullable(rowToUserProfile(row));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<UserProfileView> findProfileById(final Long githubUserId) {
        try {
            final UserProfileEntity row =
                    (UserProfileEntity) entityManager.createNativeQuery(SELECT_USER_PROFILE_WHERE_GITHUB_ID,
                                    UserProfileEntity.class)
                            .setParameter("githubUserId", githubUserId)
                            .getSingleResult();
            return Optional.ofNullable(rowToUserProfile(row));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<UserProfileView> findProfileByLogin(String githubLogin) {
        try {
            final UserProfileEntity row =
                    (UserProfileEntity) entityManager.createNativeQuery(SELECT_USER_PROFILE_WHERE_GITHUB_LOGIN,
                                    UserProfileEntity.class)
                            .setParameter("githubLogin", githubLogin)
                            .getSingleResult();
            return Optional.ofNullable(rowToUserProfile(row));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<ProjectStatsForUserEntity> getProjectsStatsForUser(final Long githubUserId) {
        return entityManager.createNativeQuery(GET_PROJECT_STATS_BY_USER, ProjectStatsForUserEntity.class)
                .setParameter("githubUserId", githubUserId)
                .getResultList();
    }
}
