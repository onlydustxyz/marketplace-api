package onlydust.com.marketplace.project.domain.view;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.net.URI;
import java.util.UUID;

@Value
@Builder
@Accessors(fluent = true)
public class CurrencyView {
    @NonNull Id id;
    @NonNull String name;
    @NonNull String code;
    @NonNull Integer decimals;
    URI logoUrl;

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
