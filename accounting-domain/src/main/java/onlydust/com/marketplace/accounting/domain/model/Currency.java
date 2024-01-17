package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
public class Currency {
    @EqualsAndHashCode.Include
    @NonNull
    private final Id id;
    @NonNull
    private final String name;
    @NonNull
    private final Code code;
    @NonNull
    private final Type type;
    private final Standard standard;
    @NonNull
    private Integer decimals;
    private final Metadata metadata;
    private final ERC20 erc20;

    public static Currency of(final @NonNull ERC20 token) {
        return Currency.builder()
                .id(Id.random())
                .name(token.name())
                .code(Code.of(token.symbol()))
                .type(Type.CRYPTO)
                .standard(Standard.ERC20)
                .erc20(token)
                .decimals(token.decimals())
                .build();
    }

    public static Currency fiat(String name, Code code, Integer decimals) {
        return Currency.builder()
                .id(Id.random())
                .name(name)
                .code(code)
                .type(Type.FIAT)
                .standard(Standard.ISO4217)
                .decimals(decimals)
                .build();
    }

    public static Currency crypto(String name, Code code, Integer decimals) {
        return Currency.builder()
                .id(Id.random())
                .name(name)
                .code(code)
                .type(Type.CRYPTO)
                .decimals(decimals)
                .build();
    }

    public Currency withMetadata(final @NonNull Metadata metadata) {
        return toBuilder().metadata(metadata).build();
    }

    public Id id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Code code() {
        return code;
    }

    public Type type() {
        return type;
    }

    public Optional<Standard> standard() {
        return Optional.ofNullable(standard);
    }

    public Integer decimals() {
        return decimals;
    }

    public Optional<String> description() {
        return Optional.ofNullable(metadata).map(Metadata::description);
    }

    public Optional<URI> logoUri() {
        return Optional.ofNullable(metadata).map(Metadata::logoUri);
    }

    public Optional<ERC20> erc20() {
        return Optional.ofNullable(erc20);
    }

    @Override
    public String toString() {
        return code.toString();
    }

    public Currency withERC20(ERC20 token) {
        return toBuilder().erc20(token).build();
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

    public record Metadata(String name, String description, @NonNull URI logoUri) {
    }

    public enum Type {FIAT, CRYPTO}

    public enum Standard {ISO4217, ERC20}
}
