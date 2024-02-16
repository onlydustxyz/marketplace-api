package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Accessors(chain = true, fluent = true)
public class Invoice {
    private final @NonNull Id id;
    private final @NonNull Invoice.Name name;
    private final @NonNull ZonedDateTime createdAt;
    private final @NonNull Money totalAfterTax; // TODO: remove in new accounting as it will be fixed at reward level
    private final @NonNull Status status;
    private final @NonNull Set<RewardId> rewards;

    public static Invoice of(InvoicePreview preview) {
        return new Invoice(
                preview.id(),
                preview.name(),
                preview.createdAt(),
                preview.totalAfterTax(),
                Status.DRAFT,
                preview.rewards().stream().map(InvoicePreview.Reward::id).collect(Collectors.toSet())
        );
    }

    public enum Status {
        DRAFT, PROCESSING, REJECTED, APPROVED;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode
    @Getter
    @Accessors(fluent = true)
    @ToString
    public static class Name {
        private final @NonNull String value;

        public static Name of(Integer sequenceNumber, String... parts) {
            return new Name("OD-%s-%03d".formatted(normalize(parts), sequenceNumber));
        }

        private static String normalize(String... parts) {
            return Stream.of(parts).map(Name::normalize).collect(Collectors.joining("-"));
        }

        private static String normalize(final @NonNull String part) {
            return StringUtils.stripAccents(part)
                    .replaceAll("[^a-zA-Z0-9]", "")
                    .toUpperCase();
        }
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
