package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.UUID;

@NoArgsConstructor(staticName = "random")
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class RewardId extends UuidWrapper {
    public static RewardId of(@NonNull final UUID uuid) {
        return RewardId.builder().uuid(uuid).build();
    }

    public static RewardId of(@NonNull final String uuid) {
        return RewardId.of(UUID.fromString(uuid));
    }

    public String pretty() {
        return "#" + this.value().toString().substring(0, 5).toUpperCase();
    }
}