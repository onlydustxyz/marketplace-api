package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.stream.Collectors.joining;
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
    @NonNull
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

    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    EngagementStatus engagementStatus;

    BigDecimal totalGrantedUsdAmount;
    BigDecimal totalRewardedUsdAmount;
    BigDecimal averageRewardUsdAmount;
    Integer onboardedContributorCount;
    Integer activeContributorCount;
    Integer rewardCount;
    Integer completedContributionCount;
    Integer completedIssueCount;
    Integer completedPrCount;
    Integer completedCodeReviewCount;

    BigDecimal previousPeriodTotalGrantedUsdAmount;
    BigDecimal previousPeriodTotalRewardedUsdAmount;
    BigDecimal previousPeriodAverageRewardUsdAmount;
    Integer previousPeriodOnboardedContributorCount;
    Integer previousPeriodActiveContributorCount;
    Integer previousPeriodRewardCount;
    Integer previousPeriodCompletedContributionCount;
    Integer previousPeriodCompletedIssueCount;
    Integer previousPeriodCompletedPrCount;
    Integer previousPeriodCompletedCodeReviewCount;

    @JdbcTypeCode(SqlTypes.JSON)
    List<TotalPerCurrency> rewardedPerCurrency;
    @JdbcTypeCode(SqlTypes.JSON)
    List<TotalPerCurrency> totalGrantedPerCurrency;

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

    private static BigDecimal coalescePrettyUsd(BigDecimal value) {
        return value == null ? ZERO : prettyUsd(value);
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
                        .totalUsdEquivalent(coalescePrettyUsd(availableBudget))
                        .totalPerCurrency(budget == null || budget.availableBudgetPerCurrency == null ? List.of() :
                                budget.availableBudgetPerCurrency.stream()
                                        .map(a -> {
                                                    final var conversionRate = (a.usdAmount == null || a.amount.compareTo(ZERO) == 0) ? ONE :
                                                            a.usdAmount.divide(a.amount, 2, HALF_EVEN);
                                                    return new DetailedTotalMoneyTotalPerCurrencyInner()
                                                            .currency(a.currency.toDto())
                                                            .amount(a.amount)
                                                            .prettyAmount(pretty(a.amount(), a.currency().decimals(), conversionRate))
                                                            .usdEquivalent(prettyUsd(a.usdAmount));
                                                }
                                        )
                                        .sorted(Comparator.comparing(m -> m.getCurrency().getCode()))
                                        .toList())
                )
                .percentUsedBudget(percentSpentBudget == null ? null : percentSpentBudget.setScale(2, HALF_EVEN))
                .totalGrantedUsdAmount(toDecimalNumberKpi(prettyUsd(totalGrantedUsdAmount), prettyUsd(previousPeriodTotalGrantedUsdAmount)))
                .totalRewardedUsdAmount(toDecimalNumberKpi(prettyUsd(totalRewardedUsdAmount), prettyUsd(previousPeriodTotalRewardedUsdAmount)))
                .averageRewardUsdAmount(toDecimalNumberKpi(prettyUsd(averageRewardUsdAmount), prettyUsd(previousPeriodAverageRewardUsdAmount)))
                .onboardedContributorCount(toNumberKpi(onboardedContributorCount, previousPeriodOnboardedContributorCount))
                .activeContributorCount(toNumberKpi(activeContributorCount, previousPeriodActiveContributorCount))
                .rewardCount(toNumberKpi(rewardCount, previousPeriodRewardCount))
                .contributionCount(toNumberKpi(completedContributionCount, previousPeriodCompletedContributionCount))
                .issueCount(toNumberKpi(completedIssueCount, previousPeriodCompletedIssueCount))
                .prCount(toNumberKpi(completedPrCount, previousPeriodCompletedPrCount))
                .codeReviewCount(toNumberKpi(completedCodeReviewCount, previousPeriodCompletedCodeReviewCount))
                .engagementStatus(engagementStatus)
                ;
    }

    public void toCsv(CSVPrinter csv, Set<Currency> allCurrencies) throws IOException {
        final var row = new ArrayList<Object>();
        row.add(project.getName());
        row.add(leads == null ? null : leads.stream().map(RegisteredUserResponse::getLogin).sorted().collect(joining(",")));
        row.add(categories == null ? null : categories.stream().map(ProjectCategoryResponse::getName).sorted().collect(joining(",")));
        row.add(languages == null ? null : languages.stream().map(LanguageResponse::getName).sorted().collect(joining(",")));
        row.add(ecosystems == null ? null : ecosystems.stream().map(EcosystemLinkResponse::getName).sorted().collect(joining(",")));
        row.add(programs == null ? null : programs.stream().map(ProgramLinkResponse::getName).sorted().collect(joining(",")));
        row.add(prettyUsd(availableBudget));
        row.add(percentSpentBudget == null ? null : percentSpentBudget.setScale(2, HALF_EVEN));
        row.add(prettyUsd(totalGrantedUsdAmount));
        row.add(prettyUsd(totalRewardedUsdAmount));
        row.add(prettyUsd(averageRewardUsdAmount));
        row.add(onboardedContributorCount);
        row.add(activeContributorCount);
        row.add(rewardCount);
        row.add(completedIssueCount);
        row.add(completedPrCount);
        row.add(completedCodeReviewCount);
        row.add(completedContributionCount);
        row.addAll(allCurrencies.stream()
                .flatMap(c -> Stream.of(
                        availableBudgetOfCurrency(c.id()),
                        grantedAmountOfCurrency(c.id()),
                        rewardedAmountOfCurrency(c.id())))
                .toList());
        csv.printRecord(row);
    }

    private BigDecimal availableBudgetOfCurrency(UUID currencyId) {
        return budget == null || budget.availableBudgetPerCurrency == null ? ZERO :
                budget.availableBudgetPerCurrency.stream()
                        .filter(a -> a.currency().id().equals(currencyId))
                        .map(AmountPerCurrency::amount)
                        .findFirst()
                        .orElse(ZERO);
    }

    private BigDecimal rewardedAmountOfCurrency(UUID currencyId) {
        return rewardedPerCurrency == null ? ZERO :
                rewardedPerCurrency.stream()
                        .filter(a -> a.currencyId().equals(currencyId))
                        .map(TotalPerCurrency::totalAmount)
                        .findFirst()
                        .orElse(ZERO);
    }

    private BigDecimal grantedAmountOfCurrency(UUID currencyId) {
        return totalGrantedPerCurrency == null ? ZERO :
                totalGrantedPerCurrency.stream()
                        .filter(a -> a.currencyId().equals(currencyId))
                        .map(TotalPerCurrency::totalAmount)
                        .findFirst()
                        .orElse(ZERO);
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
            Currency currency
    ) {
    }

    public record TotalPerCurrency(
            UUID currencyId,
            BigDecimal totalAmount
    ) {
    }

    public record Currency(
            UUID id,
            String code,
            String name,
            URI logoUrl,
            Integer decimals) {

        public ShortCurrencyResponse toDto() {
            return new ShortCurrencyResponse()
                    .id(id)
                    .code(code)
                    .name(name)
                    .logoUrl(logoUrl)
                    .decimals(decimals);
        }
    }
}
