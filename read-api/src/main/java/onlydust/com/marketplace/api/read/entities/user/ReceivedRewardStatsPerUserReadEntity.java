package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "received_rewards_stats_per_user", schema = "public")
public class ReceivedRewardStatsPerUserReadEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long recipientId;

    @NonNull BigDecimal usdTotal;
}
