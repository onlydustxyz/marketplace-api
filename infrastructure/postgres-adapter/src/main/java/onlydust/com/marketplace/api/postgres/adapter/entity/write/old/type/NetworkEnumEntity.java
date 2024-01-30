package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

public enum NetworkEnumEntity {
    ethereum, aptos, starknet, optimism;

    public static NetworkEnumEntity of(Blockchain blockchain) {
        return switch (blockchain) {
            case ETHEREUM -> ethereum;
            case APTOS -> aptos;
            case STARKNET -> starknet;
            case OPTIMISM -> optimism;
        };
    }

    public Blockchain toBlockchain() {
        return switch (this) {
            case ethereum -> Blockchain.ETHEREUM;
            case aptos -> Blockchain.APTOS;
            case starknet -> Blockchain.STARKNET;
            case optimism -> Blockchain.OPTIMISM;
        };
    }

    public Network toNetwork() {
        return switch (this) {
            case ethereum -> Network.ETHEREUM;
            case aptos -> Network.APTOS;
            case starknet -> Network.STARKNET;
            case optimism -> Network.OPTIMISM;
        };
    }


    public static NetworkEnumEntity of(Network network) {
        return switch (network) {
            case ETHEREUM -> ethereum;
            case APTOS -> aptos;
            case STARKNET -> starknet;
            case OPTIMISM -> optimism;
            default -> throw new IllegalStateException("Unexpected value: " + network);
        };
    }
}
