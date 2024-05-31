package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static onlydust.com.marketplace.kernel.Utils.CurrencyConversion.optimimumScale;
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
    private BigDecimal latestUsdQuote;
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

    public Optional<BigDecimal> latestUsdQuote() {
        return Optional.ofNullable(latestUsdQuote);
    }

    public List<Network> supportedNetworks() {
        return switch (type) {
            case FIAT -> List.of(Network.SEPA);
            case CRYPTO -> Stream.concat(
                            erc20.stream().map(ERC20::getBlockchain).map(Network::fromBlockchain),
                            Stream.ofNullable(nativeNetwork()))
                    .toList();
        };
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
            return new PayableCurrency(id, code(), name(), logoUri().orElse(null), type(), Standard.ISO4217, Network.SEPA, null);

        if (network.equals(nativeNetwork()))
            return new PayableCurrency(id, code(), name(), logoUri().orElse(null), type(), null, nativeNetwork(), null);

        final var erc20 = erc20().stream().filter(e -> e.getBlockchain().equals(network.blockchain().orElse(null)))
                .findFirst().orElseThrow(() -> OnlyDustException.internalServerError("Currency %s is not supported on network %s".formatted(code, network)));

        return new PayableCurrency(id, code(), name(), logoUri().orElse(null), type(), Standard.ERC20, Network.fromBlockchain(erc20.getBlockchain()),
                erc20.getAddress());
    }

    private Network nativeNetwork() {
        return switch (code.toString()) {
            case Code.ETH_STR -> Network.ETHEREUM;
            case Code.APT_STR -> Network.APTOS;
            default -> null;
        };
    }

    @Deprecated
    public Network legacyNetwork() {
        return switch (code.toString()) {
            case Currency.Code.USD_STR, Currency.Code.EUR_STR -> Network.SEPA;
            case Currency.Code.APT_STR -> Network.APTOS;
            case Currency.Code.ETH_STR, Currency.Code.LORDS_STR, Currency.Code.USDC_STR -> Network.ETHEREUM;
            case Currency.Code.OP_STR -> Network.OPTIMISM;
            case Currency.Code.STRK_STR -> Network.STARKNET;

            default -> throw new IllegalArgumentException("Currency %s not supported".formatted(code));
        };
    }

    @Override
    public Currency clone() {
        return toBuilder().id(Id.random())
                .erc20(new HashSet<>(erc20))
                .build();
    }

    public CurrencyView toView() {
        return CurrencyView.builder()
                .id(CurrencyView.Id.of(id.value()))
                .name(name)
                .code(code.toString())
                .decimals(decimals)
                .latestUsdQuote(latestUsdQuote)
                .logoUrl(logoUri().orElse(null))
                .build();
    }

    public int precision() {
        return optimimumScale(latestUsdQuote, decimals);
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
        public final static String EUR_STR = "EUR";
        public final static String USD_STR = "USD";
        public final static String ETH_STR = "ETH";
        public final static String APT_STR = "APT";
        public final static String STRK_STR = "STRK";
        public final static String OP_STR = "OP";
        public final static String USDC_STR = "USDC";
        public final static String LORDS_STR = "LORDS";

        public final static Code EUR = Code.of(EUR_STR);
        public final static Code USD = Code.of(USD_STR);
        public final static Code ETH = Code.of(ETH_STR);
        public final static Code APT = Code.of(APT_STR);
        public final static Code STRK = Code.of(STRK_STR);
        public final static Code OP = Code.of(OP_STR);
        public final static Code USDC = Code.of(USDC_STR);

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
