package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.BiContributorsStatsListItemResponse;
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
public class AggregatedKpisReadEntity {
    @Id
    @NonNull
    @Getter
    ZonedDateTime timestamp;

    @NonNull
    @Getter
    ZonedDateTime timestampOfPreviousPeriod;

    Integer totalCount;
    Integer newCount;
    Integer reactivatedCount;
    Integer mergedPrCount;
    BigDecimal totalGrantedUsdAmount;
    BigDecimal totalRewardedUsdAmount;

    Integer totalCount() {
        return Optional.ofNullable(totalCount).orElse(0);
    }

    Integer newCount() {
        return Optional.ofNullable(newCount).orElse(0);
    }

    Integer reactivatedCount() {
        return Optional.ofNullable(reactivatedCount).orElse(0);
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

    public BiProjectsStatsListItemResponse toProjectDto(AggregatedKpisReadEntity statsOfPreviousTimeGroup) {
        return new BiProjectsStatsListItemResponse()
                .timestamp(timestamp)
                .totalGranted(totalGrantedUsdAmount())
                .totalRewarded(totalRewardedUsdAmount())
                .mergedPrCount(mergedPrCount())
                .newProjectCount(newCount())
                .activeProjectCount(totalCount() - newCount() - reactivatedCount())
                .reactivatedProjectCount(reactivatedCount())
                .churnedProjectCount(statsOfPreviousTimeGroup == null ? 0 :
                        statsOfPreviousTimeGroup.totalCount() - totalCount() + newCount() + reactivatedCount());
    }

    public BiContributorsStatsListItemResponse toContributorDto(AggregatedKpisReadEntity statsOfPreviousTimeGroup) {
        return new BiContributorsStatsListItemResponse()
                .timestamp(timestamp)
                .totalGranted(totalGrantedUsdAmount())
                .totalRewarded(totalRewardedUsdAmount())
                .mergedPrCount(mergedPrCount())
                .newContributorCount(newCount())
                .activeContributorCount(totalCount() - newCount() - reactivatedCount())
                .reactivatedContributorCount(reactivatedCount())
                .churnedContributorCount(statsOfPreviousTimeGroup == null ? 0 :
                        statsOfPreviousTimeGroup.totalCount() - totalCount() + newCount() + reactivatedCount());
    }
}
