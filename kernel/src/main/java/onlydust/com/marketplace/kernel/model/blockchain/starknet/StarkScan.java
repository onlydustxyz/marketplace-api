package onlydust.com.marketplace.kernel.model.blockchain.starknet;

import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;

import java.net.URI;

public class StarkScan implements BlockExplorer<StarknetTransaction.Hash> {
    private static final String BASE_URL = "https://starkscan.co";

    @Override
    public URI url(StarknetTransaction.Hash transactionHash) {
        return URI.create(BASE_URL + "/tx/" + transactionHash);
    }
}
