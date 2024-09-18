package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class ProjectKpisReadEntity {
    @Id
    @NonNull
    UUID projectId;

    @JdbcTypeCode(SqlTypes.JSON)
    ProjectLinkResponse project;
    @JdbcTypeCode(SqlTypes.JSON)
    List<RegisteredUserResponse> leads;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageResponse> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    List<EcosystemLinkResponse> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProgramLinkResponse> programs;

    //    @OneToMany(mappedBy = "projectId", fetch = FetchType.LAZY)
    //    @NonNull
    //    Set<ProjectStatPerCurrencyReadEntity> globalStatsPerCurrency;

    BigDecimal totalGrantedUsdAmount;
    BigDecimal totalRewardedUsdAmount;
    BigDecimal averageRewardUsdAmount;
    Integer onboardedContributorCount;
    Integer activeContributorCount;
    Integer mergedPrCount;
    Integer rewardCount;
    Integer contributionCount;

    BigDecimal previousPeriodTotalGrantedUsdAmount;
    BigDecimal previousPeriodTotalRewardedUsdAmount;
    BigDecimal previousPeriodAverageRewardUsdAmount;
    Integer previousPeriodOnboardedContributorCount;
    Integer previousPeriodActiveContributorCount;
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

    public BiProjectsPageItemResponse toDto() {
        return new BiProjectsPageItemResponse()
                .project(project)
                .projectLeads(leads)
                .categories(categories)
                .languages(languages)
                .ecosystems(ecosystems)
                .programs(programs)
                .totalGrantedUsdAmount(toDecimalNumberKpi(totalGrantedUsdAmount, previousPeriodTotalGrantedUsdAmount))
                .totalRewardedUsdAmount(toDecimalNumberKpi(totalRewardedUsdAmount, previousPeriodTotalRewardedUsdAmount))
                .averageRewardUsdAmount(toDecimalNumberKpi(averageRewardUsdAmount, previousPeriodAverageRewardUsdAmount))
                .onboardedContributorCount(toNumberKpi(onboardedContributorCount, previousPeriodOnboardedContributorCount))
                .activeContributorCount(toNumberKpi(activeContributorCount, previousPeriodActiveContributorCount))
                .mergedPrCount(toNumberKpi(mergedPrCount, previousPeriodMergedPrCount))
                .rewardCount(toNumberKpi(rewardCount, previousPeriodRewardCount))
                .contributionCount(toNumberKpi(contributionCount, previousPeriodContributionCount))
                ;
    }
}
