package onlydust.com.marketplace.api.postgres.adapter.entity.enums;

import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

public enum NetworkEnumEntity {
    SEPA, ETHEREUM, APTOS, STARKNET, OPTIMISM, STELLAR;

    public static NetworkEnumEntity of(Blockchain blockchain) {
        return switch (blockchain) {
            case ETHEREUM -> ETHEREUM;
            case APTOS -> APTOS;
            case STARKNET -> STARKNET;
            case OPTIMISM -> OPTIMISM;
            case STELLAR -> STELLAR;
        };
    }

    public Blockchain toBlockchain() {
        return switch (this) {
            case ETHEREUM -> Blockchain.ETHEREUM;
            case APTOS -> Blockchain.APTOS;
            case STARKNET -> Blockchain.STARKNET;
            case OPTIMISM -> Blockchain.OPTIMISM;
            case STELLAR -> Blockchain.STELLAR;
            case SEPA -> throw new IllegalStateException("No blockchain equivalent found for network %s".formatted(this.name()));
        };
    }

    public Network toNetwork() {
        return switch (this) {
            case ETHEREUM -> Network.ETHEREUM;
            case APTOS -> Network.APTOS;
            case STARKNET -> Network.STARKNET;
            case OPTIMISM -> Network.OPTIMISM;
            case SEPA -> Network.SEPA;
            case STELLAR -> Network.STELLAR;
        };
    }


    public static NetworkEnumEntity of(Network network) {
        return switch (network) {
            case ETHEREUM -> ETHEREUM;
            case APTOS -> APTOS;
            case STARKNET -> STARKNET;
            case OPTIMISM -> OPTIMISM;
            case SEPA -> SEPA;
            case STELLAR -> STELLAR;
        };
    }
}
