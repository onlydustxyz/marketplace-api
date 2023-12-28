package onlydust.com.marketplace.api.domain.model.blockchain;


import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.blockchain.aptos.*;

public interface Aptos {
    BlockExplorer<TransactionHash> BLOCK_EXPLORER = new AptoScan();

    static TransactionHash transactionHash(String value) {
        return new TransactionHash(value);
    }

    static AccountAddress accountAddress(String value) {
        return new AccountAddress(value);
    }
}
