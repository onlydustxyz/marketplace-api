package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.contract.model.*;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
    @NonNull ContributorResponse contributor;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectLinkResponse> projects;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageResponse> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    List<EcosystemLinkResponse> ecosystems;
    String contributorCountry;

    BigDecimal totalRewardedUsdAmount;
    Integer mergedPrCount;
    Integer rewardCount;
    Integer contributionCount;

    BigDecimal previousPeriodTotalRewardedUsdAmount;
    Integer previousPeriodMergedPrCount;
    Integer previousPeriodRewardCount;
    Integer previousPeriodContributionCount;

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
                .countryCode(contributorCountry == null ? null : Country.fromIso3(contributorCountry).iso2Code())
                .totalRewardedUsdAmount(toDecimalNumberKpi(prettyUsd(totalRewardedUsdAmount), prettyUsd(previousPeriodTotalRewardedUsdAmount)))
                .mergedPrCount(toNumberKpi(mergedPrCount, previousPeriodMergedPrCount))
                .rewardCount(toNumberKpi(rewardCount, previousPeriodRewardCount))
                .contributionCount(toNumberKpi(contributionCount, previousPeriodContributionCount))
                ;
    }

    public void toCsv(CSVPrinter csv) throws IOException {
        csv.printRecord(
                contributor.getLogin(),
                projects == null ? null : projects.stream().map(ProjectLinkResponse::getName).sorted().collect(joining(",")),
                categories == null ? null : categories.stream().map(ProjectCategoryResponse::getName).sorted().collect(joining(",")),
                languages == null ? null : languages.stream().map(LanguageResponse::getName).sorted().collect(joining(",")),
                ecosystems == null ? null : ecosystems.stream().map(EcosystemLinkResponse::getName).sorted().collect(joining(",")),
                contributorCountry == null ? null : Country.fromIso3(contributorCountry).iso2Code(),
                prettyUsd(totalRewardedUsdAmount),
                mergedPrCount,
                rewardCount,
                contributionCount
        );
    }
}
