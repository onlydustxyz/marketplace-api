package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.BiProjectsStatsListItemResponse;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class BiAggregatedProjectGrantStatsReadEntity {
    @Id
    @NonNull
    ZonedDateTime timestamp;

    BigDecimal totalGrantedUsdAmount;

    public BiProjectsStatsListItemResponse toDto() {
        return new BiProjectsStatsListItemResponse()
                .timestamp(timestamp)
                .totalGranted(totalGrantedUsdAmount);
    }
}
