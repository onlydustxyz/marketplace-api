package onlydust.com.marketplace.api.domain.model.blockchain.evm.ethereum;

import java.net.URI;
import onlydust.com.marketplace.api.domain.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.api.domain.model.blockchain.evm.TransactionHash;

public class EtherScan implements BlockExplorer<TransactionHash> {

  private static final String BASE_URL = "https://etherscan.io";

  @Override
  public URI url(TransactionHash transactionHash) {
    return URI.create(BASE_URL + "/tx/" + transactionHash.asString());
  }
}
