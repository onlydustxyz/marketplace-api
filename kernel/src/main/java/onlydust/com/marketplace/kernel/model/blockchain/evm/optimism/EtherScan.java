package onlydust.com.marketplace.kernel.model.blockchain.evm.optimism;

import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;

import java.net.URI;

public class EtherScan implements BlockExplorer<EvmTransaction.Hash> {
    private static final String BASE_URL = "https://optimistic.etherscan.io";

    @Override
    public URI url(EvmTransaction.Hash transactionHash) {
        return URI.create(BASE_URL + "/tx/" + transactionHash);
    }
}
