package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static onlydust.com.marketplace.api.read.entities.user.PublicUserProfileResponseV2Entity.prettyRankPercentile;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

@Entity
@NoArgsConstructor(force = true)
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class ContributorKpisReadEntity {
    @Id
    @NonNull
    Long contributorId;

    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    ContributorOverviewResponse contributor;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectLinkResponse> projects;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageResponse> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    List<EcosystemLinkResponse> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectContributorLabelResponse> projectContributorLabels;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectLinkResponse> maintainedProjects;
    String contributorCountry;

    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ContributorActivityStatus activityStatus;

    BigDecimal totalRewardedUsdAmount;
    Integer rewardCount;
    Integer completedContributionCount;
    Integer completedIssueCount;
    Integer completedPrCount;
    Integer completedCodeReviewCount;
    Integer inProgressIssueCount;
    Integer pendingApplicationCount;

    BigDecimal previousPeriodTotalRewardedUsdAmount;
    Integer previousPeriodRewardCount;
    Integer previousPeriodCompletedContributionCount;
    Integer previousPeriodCompletedIssueCount;
    Integer previousPeriodCompletedPrCount;
    Integer previousPeriodCompletedCodeReviewCount;
    Integer previousPeriodInProgressIssueCount;
    Integer previousPeriodPendingApplicationCount;

    private static DecimalNumberKpi toDecimalNumberKpi(BigDecimal value, BigDecimal valueOfPreviousPeriod) {
        return new DecimalNumberKpi().value(value)
                .trend(valueOfPreviousPeriod == null ? null :
                        valueOfPreviousPeriod.compareTo(value) < 0 ? Trend.UP :
                                valueOfPreviousPeriod.compareTo(value) > 0 ? Trend.DOWN :
                                        Trend.STABLE);
    }

    private static NumberKpi toNumberKpi(Integer value, Integer valueOfPreviousPeriod) {
        return new NumberKpi().value(value)
                .trend(valueOfPreviousPeriod == null ? null :
                        valueOfPreviousPeriod.compareTo(value) < 0 ? Trend.UP :
                                valueOfPreviousPeriod.compareTo(value) > 0 ? Trend.DOWN :
                                        Trend.STABLE);
    }

    private ContributorOverviewResponse pretty(ContributorOverviewResponse contributor) {
        if (contributor.getGlobalRankPercentile() != null)
            contributor.globalRankPercentile(prettyRankPercentile(contributor.getGlobalRankPercentile()));

        return contributor;
    }

    private Optional<Country> contributorCountry() {
        return Optional.ofNullable(contributorCountry).map(Country::fromIso3);
    }

    public BiContributorsPageItemResponse toDto() {
        return new BiContributorsPageItemResponse()
                .contributor(pretty(contributor))
                .activityStatus(activityStatus)
                .projects(projects == null ? null : projects.stream().sorted(comparing(ProjectLinkResponse::getName)).toList())
                .categories(categories == null ? null : categories.stream().sorted(comparing(ProjectCategoryResponse::getName)).toList())
                .languages(languages == null ? null : languages.stream().sorted(comparing(LanguageResponse::getName)).toList())
                .ecosystems(ecosystems == null ? null : ecosystems.stream().sorted(comparing(EcosystemLinkResponse::getName)).toList())
                .projectContributorLabels(projectContributorLabels == null ? null :
                        projectContributorLabels.stream().sorted(comparing(ProjectContributorLabelResponse::getName)).toList())
                .country(contributorCountry().map(country -> new CountryResponse()
                                .code(country.iso2Code())
                                .name(country.display().orElse(null)))
                        .orElse(null))
                .totalRewardedUsdAmount(toDecimalNumberKpi(prettyUsd(totalRewardedUsdAmount), prettyUsd(previousPeriodTotalRewardedUsdAmount)))
                .rewardCount(toNumberKpi(rewardCount, previousPeriodRewardCount))
                .contributionCount(toNumberKpi(completedContributionCount, previousPeriodCompletedContributionCount))
                .issueCount(toNumberKpi(completedIssueCount, previousPeriodCompletedIssueCount))
                .prCount(toNumberKpi(completedPrCount, previousPeriodCompletedPrCount))
                .codeReviewCount(toNumberKpi(completedCodeReviewCount, previousPeriodCompletedCodeReviewCount))
                .inProgressIssueCount(inProgressIssueCount)
                .pendingApplicationCount(pendingApplicationCount)
                .maintainedProjectCount(maintainedProjects == null ? 0 : maintainedProjects.size())
                ;
    }

    public void toCsv(CSVPrinter csv) throws IOException {
        csv.printRecord(
                contributor.getLogin(),
                projects == null ? null : projects.stream().map(ProjectLinkResponse::getName).sorted().collect(joining(",")),
                categories == null ? null : categories.stream().map(ProjectCategoryResponse::getName).sorted().collect(joining(",")),
                languages == null ? null : languages.stream().map(LanguageResponse::getName).sorted().collect(joining(",")),
                ecosystems == null ? null : ecosystems.stream().map(EcosystemLinkResponse::getName).sorted().collect(joining(",")),
                projectContributorLabels == null ? null :
                        projectContributorLabels.stream().map(ProjectContributorLabelResponse::getName).sorted().collect(joining(",")),
                contributorCountry == null ? null : Country.fromIso3(contributorCountry).iso2Code(),
                prettyUsd(totalRewardedUsdAmount),
                rewardCount,
                completedIssueCount,
                completedPrCount,
                completedCodeReviewCount,
                completedContributionCount
        );
    }

    public IssueApplicantsPageItemResponse toIssueApplicant(ApplicationReadEntity application) {

        return new IssueApplicantsPageItemResponse()
                .applicationId(application.id())
                .appliedAt(application.receivedAt())
                .contributor(pretty(contributor))
                .projects(projects == null ? null : projects.stream().sorted(comparing(ProjectLinkResponse::getName)).toList())
                .categories(categories == null ? null : categories.stream().sorted(comparing(ProjectCategoryResponse::getName)).toList())
                .languages(languages == null ? null : languages.stream().sorted(comparing(LanguageResponse::getName)).toList())
                .ecosystems(ecosystems == null ? null : ecosystems.stream().sorted(comparing(EcosystemLinkResponse::getName)).toList())
                .projectContributorLabels(projectContributorLabels == null ? null :
                        projectContributorLabels.stream().sorted(comparing(ProjectContributorLabelResponse::getName)).toList())
                .country(contributorCountry().map(country -> new CountryResponse()
                                .code(country.iso2Code())
                                .name(country.display().orElse(null)))
                        .orElse(null))
                .totalRewardedUsdAmount(toDecimalNumberKpi(prettyUsd(totalRewardedUsdAmount), prettyUsd(previousPeriodTotalRewardedUsdAmount)))
                .rewardCount(toNumberKpi(rewardCount, previousPeriodRewardCount))
                .contributionCount(toNumberKpi(completedContributionCount, previousPeriodCompletedContributionCount))
                .issueCount(toNumberKpi(completedIssueCount, previousPeriodCompletedIssueCount))
                .prCount(toNumberKpi(completedPrCount, previousPeriodCompletedPrCount))
                .codeReviewCount(toNumberKpi(completedCodeReviewCount, previousPeriodCompletedCodeReviewCount))
                .inProgressIssueCount(inProgressIssueCount)
                .pendingApplicationCount(pendingApplicationCount)
                .maintainedProjectCount(maintainedProjects == null ? 0 : maintainedProjects.size())
                ;
    }
}
