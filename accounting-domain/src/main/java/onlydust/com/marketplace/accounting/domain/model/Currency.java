package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
public class Currency implements Cloneable {
    @EqualsAndHashCode.Include
    @NonNull
    private final Id id;
    @NonNull
    private final String name;
    @NonNull
    private final Code code;
    @NonNull
    private final Type type;
    @NonNull
    private Integer decimals;
    private final Metadata metadata;
    @Builder.Default
    private final Set<ERC20> erc20 = Set.of();

    public static Currency of(final @NonNull ERC20 token) {
        return Currency.builder()
                .id(Id.random())
                .name(token.getName())
                .code(Code.of(token.getSymbol()))
                .type(Type.CRYPTO)
                .erc20(new HashSet<>(Set.of(token)))
                .decimals(token.getDecimals())
                .build();
    }

    public static Currency fiat(String name, Code code, Integer decimals) {
        return Currency.builder()
                .id(Id.random())
                .name(name)
                .code(code)
                .type(Type.FIAT)
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

    public Currency withName(final @NonNull String name) {
        return toBuilder().name(name).build();
    }

    public Currency withDecimals(final @NonNull Integer decimals) {
        return toBuilder().decimals(decimals).build();
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

    public Integer decimals() {
        return decimals;
    }

    public Optional<String> description() {
        return Optional.ofNullable(metadata).map(Metadata::description);
    }

    public Optional<URI> logoUri() {
        return Optional.ofNullable(metadata).map(Metadata::logoUri);
    }

    public Set<ERC20> erc20() {
        return erc20;
    }

    @Override
    public String toString() {
        return code.toString();
    }

    public Currency withERC20(ERC20 token) {
        final var erc20 = new HashSet<>(this.erc20);
        if (!erc20.add(token))
            throw badRequest("ERC20 token at address %s on %s is already supported".formatted(token.getAddress(), token.getBlockchain().pretty()));
        return toBuilder().erc20(erc20).build();
    }

    public PayableCurrency forNetwork(final @NonNull Network network) {
        if (!type.equals(network.type()))
            throw OnlyDustException.internalServerError("Currency %s is not supported on network %s".formatted(code, network));

        if (type == Type.FIAT)
            return new PayableCurrency(id, code(), name(), logoUri().orElse(null), type(), Standard.ISO4217, null, null);

        if (network.equals(nativeNetwork()))
            return new PayableCurrency(id, code(), name(), logoUri().orElse(null), type(), null, nativeNetwork().blockchain(), null);

        final var erc20 = erc20().stream().filter(e -> e.getBlockchain().equals(network.blockchain()))
                .findFirst().orElseThrow(() -> OnlyDustException.internalServerError("Currency %s is not supported on network %s".formatted(code, network)));

        return new PayableCurrency(id, code(), name(), logoUri().orElse(null), type(), Standard.ERC20, erc20.getBlockchain(), erc20.getAddress());
    }

    private Network nativeNetwork() {
        return switch (code.toString()) {
            case Code.ETH -> Network.ETHEREUM;
            case Code.APT -> Network.APTOS;
            case Code.STRK -> Network.STARKNET;
            case Code.OP -> Network.OPTIMISM;
            default -> null;
        };
    }

    @Override
    public Currency clone() {
        return toBuilder().id(Id.random())
                .erc20(new HashSet<>(erc20))
                .build();
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
        public final static String USD = "USD";
        public final static String ETH = "ETH";
        public final static String APT = "APT";
        public final static String STRK = "STRK";
        public final static String OP = "OP";

        String inner;

        @Override
        public String toString() {
            return inner;
        }
    }

    public record Metadata(String name, String description, URI logoUri) {
    }

    public enum Type {FIAT, CRYPTO}

    public enum Standard {ISO4217, ERC20}
}
