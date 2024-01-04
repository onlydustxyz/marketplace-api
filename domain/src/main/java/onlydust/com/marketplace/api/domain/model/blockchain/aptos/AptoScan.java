package onlydust.com.marketplace.api.domain.model.blockchain.aptos;

import java.net.URI;
import onlydust.com.marketplace.api.domain.model.blockchain.BlockExplorer;

public class AptoScan implements BlockExplorer<TransactionHash> {

  private static final String BASE_URL = "https://aptoscan.com";

  @Override
  public URI url(TransactionHash version) {
    return URI.create(BASE_URL + "/version/" + version.asString());
  }
}
