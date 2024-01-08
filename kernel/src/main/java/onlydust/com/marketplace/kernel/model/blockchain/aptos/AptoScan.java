package onlydust.com.marketplace.kernel.model.blockchain.aptos;

import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;

import java.net.URI;

public class AptoScan implements BlockExplorer<TransactionHash> {
    private static final String BASE_URL = "https://aptoscan.com";

    @Override
    public URI url(TransactionHash version) {
        return URI.create(BASE_URL + "/version/" + version.asString());
    }
}
