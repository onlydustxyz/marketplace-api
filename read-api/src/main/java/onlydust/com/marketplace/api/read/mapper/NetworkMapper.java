package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.backoffice.api.contract.model.TransactionNetwork;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;

public interface NetworkMapper {
    static TransactionNetwork map(NetworkEnumEntity network) {
        return switch (network) {
            case sepa -> TransactionNetwork.SEPA;
            case ethereum -> TransactionNetwork.ETHEREUM;
            case aptos -> TransactionNetwork.APTOS;
            case starknet -> TransactionNetwork.STARKNET;
            case optimism -> TransactionNetwork.OPTIMISM;
        };
    }
}
