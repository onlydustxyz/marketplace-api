package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.ethereum;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;

import java.net.URI;

@AllArgsConstructor
public class EtherScan implements BlockExplorer<EvmTransaction.Hash> {
    private final BlockExplorerProperties properties;

    private String baseUrl() {
        return switch (properties.getEnvironment()) {
            case MAINNET -> "https://etherscan.io";
            case TESTNET -> "https://sepolia.etherscan.io";
        };
    }

    @Override
    public URI url(final EvmTransaction.Hash transactionHash) {
        return URI.create(baseUrl() + "/tx/" + transactionHash);
    }
}
