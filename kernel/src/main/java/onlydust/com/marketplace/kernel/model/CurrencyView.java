package onlydust.com.marketplace.kernel.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
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
    BigDecimal latestUsdQuote;
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
