package onlydust.com.marketplace.kernel.model.blockchain.aptos;

import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;

import java.net.URI;

public class AptoScan implements BlockExplorer<AptosTransaction.Hash> {
    private static final String BASE_URL = "https://aptoscan.com";

    @Override
    public URI url(AptosTransaction.Hash version) {
        return URI.create(BASE_URL + "/version/" + version.toString());
    }
}
