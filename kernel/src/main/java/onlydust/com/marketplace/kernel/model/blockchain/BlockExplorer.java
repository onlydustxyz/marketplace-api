package onlydust.com.marketplace.kernel.model.blockchain;

import java.net.URI;

public interface BlockExplorer<TransactionHash> {
    URI url(TransactionHash hash);
}
