package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.backoffice.api.contract.model.TransactionNetwork;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;

public interface NetworkMapper {
    static TransactionNetwork map(NetworkEnumEntity network) {
        return network == null ? null : switch (network) {
            case SEPA -> TransactionNetwork.SEPA;
            case ETHEREUM -> TransactionNetwork.ETHEREUM;
            case APTOS -> TransactionNetwork.APTOS;
            case STARKNET -> TransactionNetwork.STARKNET;
            case OPTIMISM -> TransactionNetwork.OPTIMISM;
            case STELLAR -> TransactionNetwork.STELLAR;
        };
    }
}
