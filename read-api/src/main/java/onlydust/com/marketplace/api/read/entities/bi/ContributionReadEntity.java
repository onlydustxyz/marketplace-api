package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@NoArgsConstructor(force = true)
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@IdClass(ContributionReadEntity.PrimaryKey.class)
public class ContributionReadEntity {
    @Id
    @NonNull
    Long githubId;

    @Id
    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ContributionType contributionType;

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

    @JdbcTypeCode(SqlTypes.JSON)
    ProjectLinkResponse project;

    @JdbcTypeCode(SqlTypes.JSON)
    List<GithubUserResponse> contributors;

    @JdbcTypeCode(SqlTypes.JSON)
    List<GithubUserResponse> applicants;

    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageResponse> languages;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ContributionShortLinkResponse> linkedIssues;

    @JdbcTypeCode(SqlTypes.JSON)
    BigDecimal totalRewardedUsdAmount;

    public ContributionActivityPageItemResponse toDto() {
        return new ContributionActivityPageItemResponse()
                .type(contributionType)
                .repo(githubRepo)
                .githubAuthor(githubAuthor)
                .githubId(githubId)
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
                .languages(languages)
                .linkedIssues(linkedIssues)
                .totalRewardedUsdAmount(totalRewardedUsdAmount)
                ;
    }

    @EqualsAndHashCode
    public static class PrimaryKey {
        Long githubId;
        ContributionType contributionType;
    }
}
