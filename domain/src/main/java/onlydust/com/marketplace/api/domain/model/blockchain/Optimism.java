package onlydust.com.marketplace.api.domain.model.blockchain;


import onlydust.com.marketplace.api.domain.model.blockchain.evm.AccountAddress;
import onlydust.com.marketplace.api.domain.model.blockchain.evm.TransactionHash;
import onlydust.com.marketplace.api.domain.model.blockchain.evm.optimism.EtherScan;

public interface Optimism {

  BlockExplorer<TransactionHash> BLOCK_EXPLORER = new EtherScan();

  static TransactionHash transactionHash(String value) {
    return new TransactionHash(value);
  }

  static AccountAddress accountAddress(String value) {
    return new AccountAddress(value);
  }
}
