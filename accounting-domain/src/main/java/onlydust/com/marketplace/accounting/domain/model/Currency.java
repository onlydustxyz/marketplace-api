package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Currency {
    @NonNull Id id;
    @NonNull String name;
    @NonNull Code code;
    Metadata metadata;

    public static Currency of(final @NonNull ERC20 token) {
        return new Currency(Id.random(), token.name(), Code.of(token.symbol()), null);
    }

    public Currency withMetadata(final @NonNull Metadata metadata) {
        return new Currency(id, name, code, metadata);
    }

    public Id id() { return id; }
    public String name() { return name; }
    public Code code() { return code; }
    public Optional<String> description() { return Optional.ofNullable(metadata).map(Metadata::description); }
    public Optional<URI> logoUri() { return Optional.ofNullable(metadata).map(Metadata::logoUri); }

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

    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class Code {
        public static Code USD = Code.of("USD");
        String inner;

        @Override
        public String toString() {
            return inner;
        }
    }

    public record Metadata(String description, URI logoUri) {
    }
}