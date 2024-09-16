package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.starknet;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransaction;

import java.net.URI;

@AllArgsConstructor
public class StarkScan implements BlockExplorer<StarknetTransaction.Hash> {
    private final BlockExplorerProperties properties;

    private String baseUrl() {
        return switch (properties.getEnvironment()) {
            case MAINNET -> "https://starkscan.co";
            case TESTNET -> "https://sepolia.starkscan.co";
        };
    }

    @Override
    public URI url(final StarknetTransaction.Hash transactionHash) {
        return URI.create(baseUrl() + "/tx/" + transactionHash);
    }
}
