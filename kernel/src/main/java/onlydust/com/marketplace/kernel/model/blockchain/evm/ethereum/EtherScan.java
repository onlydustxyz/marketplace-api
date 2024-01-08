package onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum;

import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.evm.TransactionHash;

import java.net.URI;

public class EtherScan implements BlockExplorer<TransactionHash> {
    private static final String BASE_URL = "https://etherscan.io";

    @Override
    public URI url(TransactionHash transactionHash) {
        return URI.create(BASE_URL + "/tx/" + transactionHash.asString());
    }
}
