package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

public enum Network {
    ETHEREUM(Currency.Type.CRYPTO, Blockchain.ETHEREUM),
    OPTIMISM(Currency.Type.CRYPTO, Blockchain.OPTIMISM),
    STARKNET(Currency.Type.CRYPTO, Blockchain.STARKNET),
    APTOS(Currency.Type.CRYPTO, Blockchain.APTOS),
    SEPA(Currency.Type.FIAT, null),
    SWIFT(Currency.Type.FIAT, null);

    private final Currency.Type type;
    private final Blockchain blockchain;

    Network(Currency.Type type, Blockchain blockchain) {
        this.type = type;
        this.blockchain = blockchain;
    }

    public Currency.Type type() {
        return type;
    }

    public Blockchain blockchain() {
        return blockchain;
    }

    @Deprecated
    public static Network fromCurrencyCode(String currencyCode) {
        return switch (currencyCode) {
            case Currency.Code.USD_STR, Currency.Code.EUR_STR -> Network.SEPA;
            case Currency.Code.APT_STR -> Network.APTOS;
            case Currency.Code.ETH_STR, Currency.Code.LORDS_STR, Currency.Code.USDC_STR -> Network.ETHEREUM;
            case Currency.Code.OP_STR -> Network.OPTIMISM;
            case Currency.Code.STRK_STR -> Network.STARKNET;

            default -> throw new IllegalArgumentException("Currency %s not supported".formatted(currencyCode));
        };
    }
}
