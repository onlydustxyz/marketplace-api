package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.BiFinancialsStatsListResponse;
import onlydust.com.marketplace.api.contract.model.BiFinancialsStatsResponse;
import onlydust.com.marketplace.api.contract.model.SortDirection;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Entity
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Builder
@Accessors(fluent = true)
public class BiFinancialMonthlyStatsReadEntity implements ProgramTransactionStat {

    @Id
    UUID technicalId;

    UUID currencyId;
    @NonNull
    ZonedDateTime date;

    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyReadEntity currency;

    @NonNull
    BigDecimal totalDeposited;

    @NonNull
    BigDecimal totalAllocated;

    @NonNull
    BigDecimal totalGranted;

    @NonNull
    BigDecimal totalRewarded;

    @NonNull
    BigDecimal totalPaid;

    @NonNull
    Integer transactionCount;

    private BiFinancialMonthlyStatsReadEntity add(BiFinancialMonthlyStatsReadEntity other) {
        return BiFinancialMonthlyStatsReadEntity.builder()
                .technicalId(technicalId)
                .currencyId(currencyId)
                .date(date)
                .currency(currency)
                .totalDeposited(totalDeposited.add(other.totalDeposited))
                .totalAllocated(totalAllocated.add(other.totalAllocated))
                .totalGranted(totalGranted.add(other.totalGranted))
                .totalRewarded(totalRewarded.add(other.totalRewarded))
                .totalPaid(totalPaid.add(other.totalPaid))
                .transactionCount(transactionCount + other.transactionCount)
                .build();
    }

    public static BiFinancialsStatsListResponse toDto(List<BiFinancialMonthlyStatsReadEntity> result, boolean showEmpty, SortDirection sortDirection) {
        final var resultPerDate = result.stream().collect(groupingBy(BiFinancialMonthlyStatsReadEntity::date));
        final var allCurrencies = result.stream().map(BiFinancialMonthlyStatsReadEntity::currency).filter(Objects::nonNull).collect(Collectors.toSet());
        final var totalsPerCurrency = result.stream().filter(e -> e.currencyId != null)
                .collect(groupingBy(BiFinancialMonthlyStatsReadEntity::currencyId)).entrySet().stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().reduce(BiFinancialMonthlyStatsReadEntity::add).orElseThrow()
                )).values();

        final List<BiFinancialsStatsResponse> responsePerMonth = new ArrayList<>();
        resultPerDate.forEach((month, monthlyStats) -> {
            allCurrencies.forEach(currency -> {
                if (monthlyStats.stream().noneMatch(s -> currency.id().equals(s.currencyId()))) {
                    monthlyStats.add(BiFinancialMonthlyStatsReadEntity.builder()
                            .date(month)
                            .currencyId(currency.id())
                            .currency(currency)
                            .totalDeposited(BigDecimal.ZERO)
                            .totalAllocated(BigDecimal.ZERO)
                            .totalGranted(BigDecimal.ZERO)
                            .totalRewarded(BigDecimal.ZERO)
                            .totalPaid(BigDecimal.ZERO)
                            .transactionCount(0)
                            .build());
                }
            });
            responsePerMonth.add(new BiFinancialsStatsResponse()
                    .date(month.toInstant().atZone(ZoneOffset.UTC).toLocalDate())
                    .totalDeposited(DetailedTotalMoneyMapper.map(monthlyStats, BiFinancialMonthlyStatsReadEntity::totalDeposited))
                    .totalAllocated(DetailedTotalMoneyMapper.map(monthlyStats, BiFinancialMonthlyStatsReadEntity::totalAllocated))
                    .totalGranted(DetailedTotalMoneyMapper.map(monthlyStats, BiFinancialMonthlyStatsReadEntity::totalGranted))
                    .totalRewarded(DetailedTotalMoneyMapper.map(monthlyStats, BiFinancialMonthlyStatsReadEntity::totalRewarded))
                    .totalPaid(DetailedTotalMoneyMapper.map(monthlyStats, BiFinancialMonthlyStatsReadEntity::totalPaid))
                    .transactionCount(monthlyStats.stream().mapToInt(BiFinancialMonthlyStatsReadEntity::transactionCount).sum()));
        });

        return new BiFinancialsStatsListResponse()
                .stats(responsePerMonth.stream()
                        .filter(r -> showEmpty || r.getTransactionCount() > 0)
                        .sorted(sortDirection == SortDirection.ASC ?
                                comparing(BiFinancialsStatsResponse::getDate) :
                                comparing(BiFinancialsStatsResponse::getDate).reversed())
                        .toList())
                .totalDeposited(DetailedTotalMoneyMapper.map(totalsPerCurrency, BiFinancialMonthlyStatsReadEntity::totalDeposited, true))
                .totalAllocated(DetailedTotalMoneyMapper.map(totalsPerCurrency, BiFinancialMonthlyStatsReadEntity::totalAllocated, true))
                .totalGranted(DetailedTotalMoneyMapper.map(totalsPerCurrency, BiFinancialMonthlyStatsReadEntity::totalGranted, true))
                .totalRewarded(DetailedTotalMoneyMapper.map(totalsPerCurrency, BiFinancialMonthlyStatsReadEntity::totalRewarded, true))
                .totalPaid(DetailedTotalMoneyMapper.map(totalsPerCurrency, BiFinancialMonthlyStatsReadEntity::totalPaid, true))
                .transactionCount(totalsPerCurrency.stream().mapToInt(BiFinancialMonthlyStatsReadEntity::transactionCount).sum());
    }
}
