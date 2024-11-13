package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.BiFinancialsStatsResponse;
import onlydust.com.marketplace.api.contract.model.SortDirection;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.mapper.DetailedTotalMoneyMapper;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

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

    public static List<BiFinancialsStatsResponse> toDto(List<BiFinancialMonthlyStatsReadEntity> result, boolean showEmpty, SortDirection sortDirection) {
        final var resultPerDate = result.stream().collect(Collectors.groupingBy(BiFinancialMonthlyStatsReadEntity::date));
        final var allCurrencies = result.stream().map(BiFinancialMonthlyStatsReadEntity::currency).filter(Objects::nonNull).collect(Collectors.toSet());

        final List<BiFinancialsStatsResponse> dto = new ArrayList<>();
        resultPerDate.forEach((date, stats) -> {
            allCurrencies.forEach(currency -> {
                if (stats.stream().noneMatch(s -> currency.id().equals(s.currencyId()))) {
                    stats.add(BiFinancialMonthlyStatsReadEntity.builder()
                            .date(date)
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
            dto.add(new BiFinancialsStatsResponse()
                    .date(date.toInstant().atZone(ZoneOffset.UTC).toLocalDate())
                    .totalDeposited(DetailedTotalMoneyMapper.map(stats, BiFinancialMonthlyStatsReadEntity::totalDeposited))
                    .totalAllocated(DetailedTotalMoneyMapper.map(stats, BiFinancialMonthlyStatsReadEntity::totalAllocated))
                    .totalGranted(DetailedTotalMoneyMapper.map(stats, BiFinancialMonthlyStatsReadEntity::totalGranted))
                    .totalRewarded(DetailedTotalMoneyMapper.map(stats, BiFinancialMonthlyStatsReadEntity::totalRewarded))
                    .totalPaid(DetailedTotalMoneyMapper.map(stats, BiFinancialMonthlyStatsReadEntity::totalPaid))
                    .transactionCount(stats.stream().mapToInt(BiFinancialMonthlyStatsReadEntity::transactionCount).sum()));
        });

        return dto.stream()
                .filter(r -> showEmpty || r.getTransactionCount() > 0)
                .sorted(sortDirection == SortDirection.ASC ?
                        comparing(BiFinancialsStatsResponse::getDate) :
                        comparing(BiFinancialsStatsResponse::getDate).reversed())
                .toList();
    }
}
