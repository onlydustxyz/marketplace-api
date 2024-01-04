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
public class CommitteeId extends UuidWrapper {
    public static CommitteeId of(@NonNull final UUID uuid) {
        return CommitteeId.builder().uuid(uuid).build();
    }

    public static CommitteeId of(@NonNull final String uuid) {
        return CommitteeId.of(UUID.fromString(uuid));
    }
}