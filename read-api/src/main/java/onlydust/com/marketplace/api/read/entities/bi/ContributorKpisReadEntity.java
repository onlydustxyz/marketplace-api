package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
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
    String contributorCountry;

    BigDecimal totalRewardedUsdAmount;
    Integer rewardCount;
    Integer contributionCount;
    Integer issueCount;
    Integer prCount;
    Integer codeReviewCount;

    BigDecimal previousPeriodTotalRewardedUsdAmount;
    Integer previousPeriodRewardCount;
    Integer previousPeriodContributionCount;
    Integer previousPeriodIssueCount;
    Integer previousPeriodPrCount;
    Integer previousPeriodCodeReviewCount;

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

    public BiContributorsPageItemResponse toDto() {
        return new BiContributorsPageItemResponse()
                .contributor(contributor)
                .projects(projects == null ? null : projects.stream().sorted(comparing(ProjectLinkResponse::getName)).toList())
                .categories(categories == null ? null : categories.stream().sorted(comparing(ProjectCategoryResponse::getName)).toList())
                .languages(languages == null ? null : languages.stream().sorted(comparing(LanguageResponse::getName)).toList())
                .ecosystems(ecosystems == null ? null : ecosystems.stream().sorted(comparing(EcosystemLinkResponse::getName)).toList())
                .projectContributorLabels(projectContributorLabels == null ? null :
                        projectContributorLabels.stream().sorted(comparing(ProjectContributorLabelResponse::getName)).toList())
                .countryCode(contributorCountry == null ? null : Country.fromIso3(contributorCountry).iso2Code())
                .totalRewardedUsdAmount(toDecimalNumberKpi(prettyUsd(totalRewardedUsdAmount), prettyUsd(previousPeriodTotalRewardedUsdAmount)))
                .rewardCount(toNumberKpi(rewardCount, previousPeriodRewardCount))
                .contributionCount(toNumberKpi(contributionCount, previousPeriodContributionCount))
                .issueCount(toNumberKpi(issueCount, previousPeriodIssueCount))
                .prCount(toNumberKpi(prCount, previousPeriodPrCount))
                .codeReviewCount(toNumberKpi(codeReviewCount, previousPeriodCodeReviewCount))
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
                issueCount,
                prCount,
                codeReviewCount,
                contributionCount
        );
    }

    public IssueApplicantsPageItemResponse toIssueApplicant(ApplicationReadEntity application) {
        return new IssueApplicantsPageItemResponse()
                .applicationId(application.id())
                .appliedAt(application.receivedAt())
                .contributor(contributor)
                .projects(projects == null ? null : projects.stream().sorted(comparing(ProjectLinkResponse::getName)).toList())
                .categories(categories == null ? null : categories.stream().sorted(comparing(ProjectCategoryResponse::getName)).toList())
                .languages(languages == null ? null : languages.stream().sorted(comparing(LanguageResponse::getName)).toList())
                .ecosystems(ecosystems == null ? null : ecosystems.stream().sorted(comparing(EcosystemLinkResponse::getName)).toList())
                .projectContributorLabels(projectContributorLabels == null ? null :
                        projectContributorLabels.stream().sorted(comparing(ProjectContributorLabelResponse::getName)).toList())
                .countryCode(contributorCountry == null ? null : Country.fromIso3(contributorCountry).iso2Code())
                .totalRewardedUsdAmount(toDecimalNumberKpi(prettyUsd(totalRewardedUsdAmount), prettyUsd(previousPeriodTotalRewardedUsdAmount)))
                .rewardCount(toNumberKpi(rewardCount, previousPeriodRewardCount))
                .contributionCount(toNumberKpi(contributionCount, previousPeriodContributionCount))
                .issueCount(toNumberKpi(issueCount, previousPeriodIssueCount))
                .prCount(toNumberKpi(prCount, previousPeriodPrCount))
                .codeReviewCount(toNumberKpi(codeReviewCount, previousPeriodCodeReviewCount))
                ;
    }
}
