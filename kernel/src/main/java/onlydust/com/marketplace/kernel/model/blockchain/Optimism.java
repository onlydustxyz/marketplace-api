package onlydust.com.marketplace.kernel.model.blockchain;


import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.TransactionHash;
import onlydust.com.marketplace.kernel.model.blockchain.evm.optimism.EtherScan;

public interface Optimism {
    BlockExplorer<TransactionHash> BLOCK_EXPLORER = new EtherScan();

    static TransactionHash transactionHash(String value) {
        return new TransactionHash(value);
    }

    static EvmAccountAddress accountAddress(String value) {
        return new EvmAccountAddress(value);
    }

    static ContractAddress contractAddress(String address) {
        return new ContractAddress(address);
    }
}
