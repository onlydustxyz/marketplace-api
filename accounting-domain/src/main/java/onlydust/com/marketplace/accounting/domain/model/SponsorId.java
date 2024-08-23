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
public class SponsorId extends UuidWrapper {
    public static SponsorId of(@NonNull final UUID uuid) {
        return SponsorId.builder().uuid(uuid).build();
    }

    public static SponsorId of(@NonNull final String uuid) {
        return SponsorId.of(UUID.fromString(uuid));
    }

    public String pretty() {
        return "#" + this.value().toString().substring(0, 5).toUpperCase();
    }
}