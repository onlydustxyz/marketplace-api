package onlydust.com.marketplace.api.read.entities.reward;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
