package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.api.contract.model.NetworkContract;

public interface NetworkMapper {
    static Network map(final @NonNull NetworkContract network) {
        return switch (network) {
            case SEPA -> Network.SEPA;
            case ETHEREUM -> Network.ETHEREUM;
            case OPTIMISM -> Network.OPTIMISM;
            case APTOS -> Network.APTOS;
            case STARKNET -> Network.STARKNET;
            case STELLAR -> Network.STELLAR;
        };
    }
}
