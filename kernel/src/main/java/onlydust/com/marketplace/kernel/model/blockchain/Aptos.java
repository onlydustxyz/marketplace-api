package onlydust.com.marketplace.kernel.model.blockchain;


import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptoScan;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.TransactionHash;

public interface Aptos {
    BlockExplorer<TransactionHash> BLOCK_EXPLORER = new AptoScan();

    static TransactionHash transactionHash(String value) {
        return new TransactionHash(value);
    }

    static AptosAccountAddress accountAddress(String value) {
        return new AptosAccountAddress(value);
    }
}
