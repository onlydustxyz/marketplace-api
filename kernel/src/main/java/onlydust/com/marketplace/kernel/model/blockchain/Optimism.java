package onlydust.com.marketplace.kernel.model.blockchain;


import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmContractAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransactionHash;
import onlydust.com.marketplace.kernel.model.blockchain.evm.optimism.EtherScan;

public interface Optimism {
    BlockExplorer<EvmTransactionHash> BLOCK_EXPLORER = new EtherScan();

    static EvmTransactionHash transactionHash(String value) {
        return new EvmTransactionHash(value);
    }

    static EvmAccountAddress accountAddress(String value) {
        return new EvmAccountAddress(value);
    }

    static EvmContractAddress contractAddress(String address) {
        return new EvmContractAddress(address);
    }
}
