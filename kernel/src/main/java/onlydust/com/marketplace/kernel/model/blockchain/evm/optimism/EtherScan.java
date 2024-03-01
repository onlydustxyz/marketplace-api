package onlydust.com.marketplace.kernel.model.blockchain.evm.optimism;

import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransactionHash;

import java.net.URI;

public class EtherScan implements BlockExplorer<EvmTransactionHash> {
    private static final String BASE_URL = "https://optimistic.etherscan.io";

    @Override
    public URI url(EvmTransactionHash transactionHash) {
        return URI.create(BASE_URL + "/tx/" + transactionHash);
    }
}
