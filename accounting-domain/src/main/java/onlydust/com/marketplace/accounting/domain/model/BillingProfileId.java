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
public class BillingProfileId extends UuidWrapper {
    public static BillingProfileId of(@NonNull final UUID uuid) {
        return BillingProfileId.builder().uuid(uuid).build();
    }

    public static BillingProfileId of(@NonNull final String uuid) {
        return BillingProfileId.of(UUID.fromString(uuid));
    }
}