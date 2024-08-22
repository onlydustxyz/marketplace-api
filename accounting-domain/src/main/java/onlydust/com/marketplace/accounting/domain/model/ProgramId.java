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
public class ProgramId extends UuidWrapper {
    public static ProgramId of(@NonNull final UUID uuid) {
        return ProgramId.builder().uuid(uuid).build();
    }

    public static ProgramId of(@NonNull final String uuid) {
        return ProgramId.of(UUID.fromString(uuid));
    }
}