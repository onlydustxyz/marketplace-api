package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class ContributionReadEntity {
    @Id
    @NonNull
    UUID contributionUuid;

    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ContributionType contributionType;

    UUID projectId;

    String githubId;

    @JdbcTypeCode(SqlTypes.JSON)
    ShortGithubRepoResponse githubRepo;

    @JdbcTypeCode(SqlTypes.JSON)
    GithubUserResponse githubAuthor;

    Long githubNumber;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    GithubStatus githubStatus;

    String githubTitle;
    String githubHtmlUrl;
    String githubBody;

    @JdbcTypeCode(SqlTypes.JSON)
    List<GithubLabel> githubLabels;

    ZonedDateTime lastUpdatedAt;
    ZonedDateTime createdAt;
    ZonedDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ContributionActivityStatus activityStatus;

    Integer githubCommentCount;

    @JdbcTypeCode(SqlTypes.JSON)
    ProjectLinkResponse project;

    @JdbcTypeCode(SqlTypes.JSON)
    List<AssignedContributorResponse> contributors;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ApplicantResponse> applicants;

    @JdbcTypeCode(SqlTypes.JSON)
    ContactableContributorResponse mergedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageResponse> languages;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ContributionShortLinkResponse> linkedIssues;

    BigDecimal totalRewardedUsdAmount;
    @JdbcTypeCode(SqlTypes.JSON)
    List<RewardedPerRecipient> rewardedPerRecipients;

    public ContributionActivityPageItemResponse toDto(Optional<AuthenticatedUser> caller) {
        return new ContributionActivityPageItemResponse()
                .uuid(contributionUuid)
                .type(contributionType)
                .githubId(githubId)
                .repo(githubRepo)
                .githubAuthor(githubAuthor)
                .githubNumber(githubNumber)
                .githubStatus(githubStatus)
                .githubTitle(githubTitle)
                .githubHtmlUrl(githubHtmlUrl)
                .githubBody(githubBody)
                .githubLabels(githubLabels)
                .lastUpdatedAt(lastUpdatedAt)
                .createdAt(createdAt)
                .completedAt(completedAt)
                .activityStatus(activityStatus)
                .project(project)
                .contributors(contributors)
                .applicants(applicants)
                .mergedBy(mergedBy)
                .languages(languages)
                .linkedIssues(linkedIssues)
                .githubCommentCount(githubCommentCount)
                .totalRewardedUsdAmount(projectId == null ? null :
                        caller.filter(u -> u.projectsLed().contains(projectId)).map(u -> totalRewardedUsdAmount).orElse(null))
                .callerTotalRewardedUsdAmount(rewardedPerRecipients == null ? null :
                        caller.flatMap(u -> rewardedPerRecipients.stream().filter(rpr -> rpr.recipientId().equals(u.githubUserId())).map(RewardedPerRecipient::totalRewardedUsdAmount).findFirst()).orElse(null))
                ;
    }

    public record RewardedPerRecipient(Long recipientId, BigDecimal totalRewardedUsdAmount) {
    }
}
