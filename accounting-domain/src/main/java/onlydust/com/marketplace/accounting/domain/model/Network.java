package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.kernel.model.blockchain.*;

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

    public static Network fromBlockchain(Blockchain blockchain) {
        if (blockchain == null)
            return Network.SEPA; //TODO: what about SWIFT?

        return switch (blockchain) {
            case ETHEREUM -> Network.ETHEREUM;
            case OPTIMISM -> Network.OPTIMISM;
            case STARKNET -> Network.STARKNET;
            case APTOS -> Network.APTOS;
        };
    }

    public void validateTransactionReference(String transactionReference) {
        switch (this) {
            case ETHEREUM -> Ethereum.transactionHash(transactionReference);
            case OPTIMISM -> Optimism.transactionHash(transactionReference);
            case STARKNET -> StarkNet.transactionHash(transactionReference);
            case APTOS -> Aptos.transactionHash(transactionReference);
            case SEPA, SWIFT -> {
                //TODO validate IBAN &co
            }
        }
    }
}
