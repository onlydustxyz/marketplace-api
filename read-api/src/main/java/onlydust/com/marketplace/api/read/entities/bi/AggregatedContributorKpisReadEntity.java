package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.BiContributorsStatsListItemResponse;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

@Entity
@NoArgsConstructor(force = true)
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class AggregatedContributorKpisReadEntity {
    @Id
    @NonNull
    ZonedDateTime timestamp;

    @NonNull
    ZonedDateTime timestampOfPreviousPeriod;

    Integer activeContributorCount;
    Integer newContributorCount;
    Integer reactivatedContributorCount;
    Integer nextPeriodChurnedContributorCount;
    Integer mergedPrCount;
    BigDecimal totalGrantedUsdAmount;
    BigDecimal totalRewardedUsdAmount;

    public BiContributorsStatsListItemResponse toDto(AggregatedContributorKpisReadEntity statsOfPreviousTimeGroup) {
        return new BiContributorsStatsListItemResponse()
                .timestamp(timestamp)
                .totalGranted(Optional.ofNullable(totalGrantedUsdAmount).orElse(BigDecimal.ZERO))
                .totalRewarded(Optional.ofNullable(totalRewardedUsdAmount).orElse(BigDecimal.ZERO))
                .mergedPrCount(Optional.ofNullable(mergedPrCount).orElse(0))
                .newContributorCount(Optional.ofNullable(newContributorCount).orElse(0))
                .activeContributorCount(Optional.ofNullable(activeContributorCount).orElse(0))
                .reactivatedContributorCount(Optional.ofNullable(reactivatedContributorCount).orElse(0))
                .churnedContributorCount(Optional.ofNullable(statsOfPreviousTimeGroup).flatMap(s -> Optional.ofNullable(s.nextPeriodChurnedContributorCount)).orElse(0));
    }
}
