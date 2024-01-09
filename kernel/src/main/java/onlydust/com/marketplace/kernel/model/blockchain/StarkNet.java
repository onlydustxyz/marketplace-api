package onlydust.com.marketplace.kernel.model.blockchain;

import onlydust.com.marketplace.kernel.model.blockchain.starknet.AccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarkScan;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.TransactionHash;

public interface StarkNet {
    BlockExplorer<TransactionHash> BLOCK_EXPLORER = new StarkScan();

    static TransactionHash transactionHash(String value) {
        return new TransactionHash(value);
    }

    static AccountAddress accountAddress(String value) {
        return new AccountAddress(value);
    }
}
