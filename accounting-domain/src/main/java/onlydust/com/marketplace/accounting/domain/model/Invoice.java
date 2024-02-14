package onlydust.com.marketplace.accounting.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true, fluent = true)
public class Invoice {
    private final @NonNull Id id;
    private final @NonNull String name;
    private final @NonNull ZonedDateTime createdAt;
    private final @NonNull Money totalAfterTax;
    private final @NonNull Status status;

    public enum Status {
        PROCESSING, REJECTED, COMPLETE;
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    }
}
