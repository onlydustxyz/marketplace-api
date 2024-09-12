package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.BiProjectsStatsListItemResponse;
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
public class AggregatedProjectKpisReadEntity {
    @Id
    @NonNull
    ZonedDateTime timestamp;

    @NonNull
    ZonedDateTime timestampOfPreviousPeriod;

    Integer activeProjectCount;
    Integer newProjectCount;
    Integer reactivatedProjectCount;
    Integer nextPeriodChurnedProjectCount;
    Integer mergedPrCount;
    BigDecimal totalGrantedUsdAmount;
    BigDecimal totalRewardedUsdAmount;

    public BiProjectsStatsListItemResponse toDto(AggregatedProjectKpisReadEntity statsOfPreviousTimeGroup) {
        return new BiProjectsStatsListItemResponse()
                .timestamp(timestamp)
                .totalGranted(Optional.ofNullable(totalGrantedUsdAmount).orElse(BigDecimal.ZERO))
                .totalRewarded(Optional.ofNullable(totalRewardedUsdAmount).orElse(BigDecimal.ZERO))
                .mergedPrCount(Optional.ofNullable(mergedPrCount).orElse(0))
                .newProjectCount(Optional.ofNullable(newProjectCount).orElse(0))
                .activeProjectCount(Optional.ofNullable(activeProjectCount).orElse(0))
                .reactivatedProjectCount(Optional.ofNullable(reactivatedProjectCount).orElse(0))
                .churnedProjectCount(Optional.ofNullable(statsOfPreviousTimeGroup).flatMap(s -> Optional.ofNullable(s.nextPeriodChurnedProjectCount)).orElse(0));
    }
}
