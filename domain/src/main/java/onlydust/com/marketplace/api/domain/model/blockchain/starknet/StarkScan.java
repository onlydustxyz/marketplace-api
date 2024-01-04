package onlydust.com.marketplace.api.domain.model.blockchain.starknet;

import java.net.URI;
import onlydust.com.marketplace.api.domain.model.blockchain.BlockExplorer;

public class StarkScan implements BlockExplorer<TransactionHash> {

  private static final String BASE_URL = "https://starkscan.co";

  @Override
  public URI url(TransactionHash transactionHash) {
    return URI.create(BASE_URL + "/tx/" + transactionHash.asString());
  }
}
