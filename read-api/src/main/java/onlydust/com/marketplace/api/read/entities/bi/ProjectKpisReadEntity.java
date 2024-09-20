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
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

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
    @JdbcTypeCode(SqlTypes.JSON)
    Budget budget;
    BigDecimal availableBudget;
    BigDecimal percentSpentBudget;

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
                .projectLeads(leads == null ? null : leads.stream().sorted(Comparator.comparing(RegisteredUserResponse::getLogin)).toList())
                .categories(categories == null ? null : categories.stream().sorted(Comparator.comparing(ProjectCategoryResponse::getName)).toList())
                .languages(languages == null ? null : languages.stream().sorted(Comparator.comparing(LanguageResponse::getName)).toList())
                .ecosystems(ecosystems == null ? null : ecosystems.stream().sorted(Comparator.comparing(EcosystemLinkResponse::getName)).toList())
                .programs(programs == null ? null : programs.stream().sorted(Comparator.comparing(ProgramLinkResponse::getName)).toList())
                .availableBudget(new DetailedTotalMoney()
                        .totalUsdEquivalent(availableBudget)
                        .totalPerCurrency(budget == null || budget.availableBudgetPerCurrency == null ? null :
                                budget.availableBudgetPerCurrency.stream()
                                        .map(a -> {
                                                    final var conversionRate = a.usdAmount == null ? BigDecimal.ONE :
                                                            a.usdAmount.divide(a.amount, 2, RoundingMode.HALF_EVEN);
                                                    return new DetailedTotalMoneyTotalPerCurrencyInner()
                                                            .currency(a.currency)
                                                            .amount(a.amount)
                                                            .prettyAmount(pretty(a.amount(), a.currency().getDecimals(), conversionRate))
                                                            .usdEquivalent(prettyUsd(a.usdAmount));
                                                }
                                        )
                                        .sorted(Comparator.comparing(m -> m.getCurrency().getCode()))
                                        .toList())
                )
                .percentUsedBudget(percentSpentBudget == null ? null : percentSpentBudget.setScale(2, RoundingMode.HALF_EVEN))
                .totalGrantedUsdAmount(toDecimalNumberKpi(prettyUsd(totalGrantedUsdAmount), prettyUsd(previousPeriodTotalGrantedUsdAmount)))
                .totalRewardedUsdAmount(toDecimalNumberKpi(prettyUsd(totalRewardedUsdAmount), prettyUsd(previousPeriodTotalRewardedUsdAmount)))
                .averageRewardUsdAmount(toDecimalNumberKpi(prettyUsd(averageRewardUsdAmount), prettyUsd(previousPeriodAverageRewardUsdAmount)))
                .onboardedContributorCount(toNumberKpi(onboardedContributorCount, previousPeriodOnboardedContributorCount))
                .activeContributorCount(toNumberKpi(activeContributorCount, previousPeriodActiveContributorCount))
                .mergedPrCount(toNumberKpi(mergedPrCount, previousPeriodMergedPrCount))
                .rewardCount(toNumberKpi(rewardCount, previousPeriodRewardCount))
                .contributionCount(toNumberKpi(contributionCount, previousPeriodContributionCount))
                ;
    }

    public record Budget(
            BigDecimal availableBudgetUsd,
            BigDecimal percentSpentBudgetUsd,
            List<AmountPerCurrency> availableBudgetPerCurrency,
            List<AmountPerCurrency> percentSpentBudgetPerCurrency,
            BigDecimal grantedAmountUsd,
            List<AmountPerCurrency> grantedAmountPerCurrency,
            BigDecimal rewardedAmountUsd,
            List<AmountPerCurrency> rewardedAmountPerCurrency
    ) {
    }

    public record AmountPerCurrency(
            BigDecimal amount,
            BigDecimal usdAmount,
            ShortCurrencyResponse currency
    ) {
    }
}
