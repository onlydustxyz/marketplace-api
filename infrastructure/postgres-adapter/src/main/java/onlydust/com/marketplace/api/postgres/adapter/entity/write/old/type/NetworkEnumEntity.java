package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

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
}
