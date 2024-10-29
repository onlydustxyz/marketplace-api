package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.near;

import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.near.NearTransaction;

import java.net.URI;

public class NearBlocks implements BlockExplorer<NearTransaction.Hash> {
    private final String baseUrl;

    public NearBlocks(final BlockExplorerProperties properties) {
        this.baseUrl = switch (properties.getEnvironment()) {
            case MAINNET -> "https://nearblocks.io";
            case TESTNET -> "https://testnet.nearblocks.io";
        };
    }

    @Override
    public URI url(final NearTransaction.Hash transactionHash) {
        return URI.create(baseUrl + "/txns/%s".formatted(transactionHash));
    }
}
