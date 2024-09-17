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
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class AggregatedContributorKpisReadEntity {
    @Id
    @NonNull
    @Getter
    ZonedDateTime timestamp;

    @NonNull
    @Getter
    ZonedDateTime timestampOfPreviousPeriod;

    Integer activeContributorCount;
    Integer newContributorCount;
    Integer reactivatedContributorCount;
    Integer mergedPrCount;
    BigDecimal totalGrantedUsdAmount;
    BigDecimal totalRewardedUsdAmount;

    Integer activeContributorCount() {
        return Optional.ofNullable(activeContributorCount).orElse(0);
    }

    Integer newContributorCount() {
        return Optional.ofNullable(newContributorCount).orElse(0);
    }

    Integer reactivatedContributorCount() {
        return Optional.ofNullable(reactivatedContributorCount).orElse(0);
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

    public BiContributorsStatsListItemResponse toDto(AggregatedContributorKpisReadEntity statsOfPreviousTimeGroup) {
        return new BiContributorsStatsListItemResponse()
                .timestamp(timestamp)
                .totalGranted(totalGrantedUsdAmount())
                .totalRewarded(totalRewardedUsdAmount())
                .mergedPrCount(mergedPrCount())
                .newContributorCount(newContributorCount())
                .activeContributorCount(activeContributorCount())
                .reactivatedContributorCount(reactivatedContributorCount())
                .churnedContributorCount(statsOfPreviousTimeGroup == null ? 0 :
                        statsOfPreviousTimeGroup.activeContributorCount() - activeContributorCount() + newContributorCount() + reactivatedContributorCount());
    }
}
