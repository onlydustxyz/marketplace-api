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
public class AccountId extends UuidWrapper {
    public static AccountId of(@NonNull final UUID uuid) {
        return AccountId.builder().uuid(uuid).build();
    }

    public static AccountId of(@NonNull final String uuid) {
        return AccountId.of(UUID.fromString(uuid));
    }
}
