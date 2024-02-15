package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

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

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode
    @Getter
    @Accessors(fluent = true)
    public static class Id {
        private final @NonNull String value;

        public static Id of(Integer sequenceNumber, String... parts) {
            return new Id("OD-" + String.join("-", normalize2(parts)) + "-" + String.format("%03d", sequenceNumber));
        }

        private static String[] normalize2(String... parts) {
            return Stream.of(parts)
                    .map(StringUtils::stripAccents)
                    .map(s -> s.replaceAll("[^a-zA-Z0-9]", ""))
                    .map(String::toUpperCase)
                    .toArray(String[]::new);
        }
    }
}
