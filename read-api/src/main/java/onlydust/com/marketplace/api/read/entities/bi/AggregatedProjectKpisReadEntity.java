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
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class AggregatedProjectKpisReadEntity {
    @Id
    @NonNull
    @Getter
    ZonedDateTime timestamp;

    @NonNull
    @Getter
    ZonedDateTime timestampOfPreviousPeriod;

    Integer activeProjectCount;
    Integer newProjectCount;
    Integer reactivatedProjectCount;
    Integer mergedPrCount;
    BigDecimal totalGrantedUsdAmount;
    BigDecimal totalRewardedUsdAmount;

    Integer activeProjectCount() {
        return Optional.ofNullable(activeProjectCount).orElse(0);
    }

    Integer newProjectCount() {
        return Optional.ofNullable(newProjectCount).orElse(0);
    }

    Integer reactivatedProjectCount() {
        return Optional.ofNullable(reactivatedProjectCount).orElse(0);
    }

    Integer mergedPrCount() {
        return Optional.ofNullable(mergedPrCount).orElse(0);
    }

    BigDecimal totalGrantedUsdAmount() {
        return Optional.ofNullable(totalGrantedUsdAmount).orElse(BigDecimal.ZERO);
    }

    BigDecimal totalRewardedUsdAmount() {
        return Optional.ofNullable(totalRewardedUsdAmount).orElse(BigDecimal.ZERO);
    }

    public BiProjectsStatsListItemResponse toDto(AggregatedProjectKpisReadEntity statsOfPreviousTimeGroup) {
        return new BiProjectsStatsListItemResponse()
                .timestamp(timestamp)
                .totalGranted(totalGrantedUsdAmount())
                .totalRewarded(totalRewardedUsdAmount())
                .mergedPrCount(mergedPrCount())
                .newProjectCount(newProjectCount())
                .activeProjectCount(activeProjectCount())
                .reactivatedProjectCount(reactivatedProjectCount())
                .churnedProjectCount(statsOfPreviousTimeGroup == null ? 0 :
                        statsOfPreviousTimeGroup.activeProjectCount() - activeProjectCount() + newProjectCount() + reactivatedProjectCount());
    }
}
