package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.stellar;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransaction;

import java.net.URI;

@AllArgsConstructor
public class StellarExpert implements BlockExplorer<StellarTransaction.Hash> {
    private final BlockExplorerProperties properties;

    private static final String BASE_URL = "https://stellar.expert/explorer";

    private String network() {
        return switch (properties.getEnvironment()) {
            case MAINNET -> "public";
            case TESTNET -> "testnet";
        };
    }

    @Override
    public URI url(final StellarTransaction.Hash transactionHash) {
        return URI.create(BASE_URL + "/%s/tx/%s".formatted(network(), transactionHash));
    }
}
