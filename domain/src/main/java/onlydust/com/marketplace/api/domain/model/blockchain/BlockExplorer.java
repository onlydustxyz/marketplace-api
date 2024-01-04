package onlydust.com.marketplace.api.domain.model.blockchain;

import java.net.URI;

public interface BlockExplorer<TransactionHash> {

  URI url(TransactionHash hash);
}
