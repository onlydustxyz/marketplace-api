package onlydust.com.marketplace.bff.read.entities.reward;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Value
@Table(name = "reward_statuses", schema = "accounting")
@NoArgsConstructor(force = true)
@Immutable
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RewardStatusReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID rewardId;

    @Enumerated(EnumType.STRING)
    @NonNull
    RewardStatus.Input status;
}
