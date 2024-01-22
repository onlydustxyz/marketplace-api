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
public class ContributorId extends UuidWrapper {
    public static ContributorId of(@NonNull final UUID uuid) {
        return ContributorId.builder().uuid(uuid).build();
    }

    public static ContributorId of(@NonNull final String uuid) {
        return ContributorId.of(UUID.fromString(uuid));
    }
}