package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.UserDetailsResponse;
import onlydust.com.backoffice.api.contract.model.UserLinkResponse;
import onlydust.com.backoffice.api.contract.model.UserPageItemResponse;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.billing_profile.BillingProfileReadEntity;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonRegistrationReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectCategoryReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SQLRestriction;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "all_users", schema = "iam")
public class AllUserReadEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long githubUserId;
    UUID userId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;

    String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserReadEntity registered;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    Set<HackathonRegistrationReadEntity> hackathonRegistrations;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    Set<ContactInformationReadEntity> contacts;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    UserProfileInfoReadEntity profile;

    public Optional<UserProfileInfoReadEntity> profile() {
        return ofNullable(profile);
    }

    @ManyToMany
    @JoinTable(
            name = "project_leads",
            schema = "public",
            joinColumns = @JoinColumn(name = "userId", referencedColumnName = "userId"),
            inverseJoinColumns = @JoinColumn(name = "projectId")
    )
    @NonNull
    Set<ProjectReadEntity> projectsLed;

    @ManyToMany
    @JoinTable(
            name = "pending_project_leader_invitations",
            schema = "public",
            joinColumns = @JoinColumn(name = "githubUserId", referencedColumnName = "githubUserId"),
            inverseJoinColumns = @JoinColumn(name = "projectId")
    )
    @NonNull
    Set<ProjectReadEntity> pendingProjectsLed;

    @ManyToMany
    @JoinTable(
            name = "sponsors_users",
            schema = "public",
            joinColumns = @JoinColumn(name = "userId", referencedColumnName = "userId"),
            inverseJoinColumns = @JoinColumn(name = "sponsorId")
    )
    @NonNull
    Set<SponsorReadEntity> sponsors;

    @OneToMany(mappedBy = "applicant")
    @NonNull
    Set<ApplicationReadEntity> applications;

    @OneToMany(mappedBy = "applicant")
    @SQLRestriction("origin = 'GITHUB' and not exists(select 1 from indexer_exp.github_issues_assignees gia where gia.issue_id = issue_id)")
    @NonNull
    Set<ApplicationReadEntity> pendingApplications;

    @ManyToMany
    @JoinTable(
            name = "billing_profiles_users",
            schema = "accounting",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "userId"),
            inverseJoinColumns = @JoinColumn(name = "billing_profile_id")
    )
    @OrderBy("name")
    @NonNull
    List<BillingProfileReadEntity> billingProfiles;

    @OneToMany
    @JoinColumn(name = "githubUserId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    @NonNull
    @Getter(AccessLevel.NONE)
    Set<GlobalUsersRanksReadEntity> globalUsersRanks;

    public Optional<GlobalUsersRanksReadEntity> globalUsersRanks() {
        return globalUsersRanks.stream().findFirst();
    }

    @OneToMany
    @JoinColumn(name = "recipientId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    @NonNull
    @Getter(AccessLevel.NONE)
    Set<ReceivedRewardStatsPerUserReadEntity> receivedRewardStats;

    public Optional<ReceivedRewardStatsPerUserReadEntity> receivedRewardStats() {
        return receivedRewardStats.stream().findFirst();
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    OnboardingCompletionEntity onboardingCompletion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    OnboardingReadEntity onboarding;

    public UserPageItemResponse toBoPageItemResponse() {
        return new UserPageItemResponse()
                .id(userId)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(ofNullable(registered).map(u -> u.lastSeenAt().toInstant().atZone(ZoneId.systemDefault())).orElse(null))
                .signedUpAt(ofNullable(registered).map(u -> u.createdAt().toInstant().atZone(ZoneId.systemDefault())).orElse(null))
                ;
    }

    public UserLinkResponse toLinkResponse() {
        return new UserLinkResponse()
                .userId(userId)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl);
    }

    public UserDetailsResponse toUserDetailsResponse() {
        return new UserDetailsResponse()
                .id(userId)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(registered == null ? null : registered.lastSeenAt())
                .signedUpAt(registered == null ? null : toZoneDateTime(registered.createdAt()))
                .contacts(ofNullable(contacts).orElse(Set.of()).stream().map(ContactInformationReadEntity::toBODto).toList())
                .leadedProjectCount(globalUsersRanks().map(GlobalUsersRanksReadEntity::leadedProjectCount).orElse(0L).intValue())
                .totalEarnedUsd(receivedRewardStats().map(ReceivedRewardStatsPerUserReadEntity::usdTotal).orElse(ZERO))
                .billingProfiles(ofNullable(billingProfiles).orElse(List.of()).stream().map(BillingProfileReadEntity::toBoShortResponse).toList())
                .leadedProjects(ofNullable(projectsLed).orElse(Set.of()).stream().map(ProjectReadEntity::toBoLinkResponse).toList())
                ;
    }

    public GithubUserResponse toGithubUserResponse() {
        return new GithubUserResponse()
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl);
    }

    public ContributorResponse toContributorResponse() {
        return new ContributorResponse()
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .isRegistered(registered != null);
    }

    public RankedContributorResponse toRankedContributorResponse() {
        return new RankedContributorResponse()
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .isRegistered(registered != null)
                .globalRank(globalUsersRanks().map(GlobalUsersRanksReadEntity::rank).orElse(0L).intValue())
                .globalRankCategory(globalUsersRanks().map(GlobalUsersRanksReadEntity::rankCategory).orElse(null))
                ;
    }

    public UserLinkResponse toBoLinkResponse() {
        return new UserLinkResponse()
                .userId(userId)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                ;
    }

    public PrivateUserProfileResponse toPrivateUserProfileResponse(List<ProjectCategoryReadEntity> preferredCategories,
                                                                   List<LanguageReadEntity> preferredLanguages) {
        return new PrivateUserProfileResponse()
                .githubUserId(githubUserId)
                .id(userId)
                .login(login)
                .avatarUrl(avatarUrl)
                .bio(profile().map(UserProfileInfoReadEntity::bio).orElse(null))
                .website(profile().map(UserProfileInfoReadEntity::website).orElse(null))
                .location(profile().map(UserProfileInfoReadEntity::location).orElse(null))
                .contactEmail(email)
                .contacts(profile().map(p -> p.allContacts().stream().map(ContactInformationReadEntity::toDto).toList()).orElse(null))
                .allocatedTimeToContribute(profile().flatMap(UserProfileInfoReadEntity::weeklyAllocatedTime).map(t -> switch (t) {
                    case none -> AllocatedTime.NONE;
                    case less_than_one_day -> AllocatedTime.LESS_THAN_ONE_DAY;
                    case one_to_three_days -> AllocatedTime.ONE_TO_THREE_DAYS;
                    case greater_than_three_days -> AllocatedTime.GREATER_THAN_THREE_DAYS;
                }).orElse(null))
                .isLookingForAJob(profile().map(UserProfileInfoReadEntity::isLookingForAJob).orElse(null))
                .firstName(profile().map(UserProfileInfoReadEntity::firstName).orElse(null))
                .lastName(profile().map(UserProfileInfoReadEntity::lastName).orElse(null))
                .joiningReason(profile().map(UserProfileInfoReadEntity::joiningReason).map(joiningReason -> switch (joiningReason) {
                    case CONTRIBUTOR -> UserJoiningReason.CONTRIBUTOR;
                    case MAINTAINER -> UserJoiningReason.MAINTAINER;
                }).orElse(null))
                .joiningGoal(profile().map(UserProfileInfoReadEntity::joiningGoal).map(joiningGoal -> switch (joiningGoal) {
                    case EARN -> UserJoiningGoal.EARN;
                    case LEARN -> UserJoiningGoal.LEARN;
                    case NOTORIETY -> UserJoiningGoal.NOTORIETY;
                    case CHALLENGE -> UserJoiningGoal.CHALLENGE;
                }).orElse(null))
                .preferredCategories(preferredCategories.stream().map(ProjectCategoryReadEntity::toDto).toList())
                .preferredLanguages(preferredLanguages.stream().map(LanguageReadEntity::toDto).toList());
    }
}
