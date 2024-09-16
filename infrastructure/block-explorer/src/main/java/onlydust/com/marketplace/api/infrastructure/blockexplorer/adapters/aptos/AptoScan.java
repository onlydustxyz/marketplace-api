package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.aptos;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosTransaction;

import java.net.URI;

@AllArgsConstructor
public class AptoScan implements BlockExplorer<AptosTransaction.Hash> {
    private final BlockExplorerProperties properties;

    private static final String BASE_URL = "https://aptoscan.com";

    private String network() {
        return switch (properties.getEnvironment()) {
            case MAINNET -> "mainnet";
            case TESTNET -> "testnet";
        };
    }

    @Override
    public URI url(final AptosTransaction.Hash hash) {
        return URI.create(BASE_URL + "/transaction/%s?network=%s".formatted(hash, network()));
    }
}
