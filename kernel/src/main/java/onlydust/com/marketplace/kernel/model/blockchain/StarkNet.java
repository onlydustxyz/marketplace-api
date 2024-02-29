package onlydust.com.marketplace.kernel.model.blockchain;

import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarkScan;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetContractAddress;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransactionHash;

public interface StarkNet {
    BlockExplorer<StarknetTransactionHash> BLOCK_EXPLORER = new StarkScan();

    static StarknetTransactionHash transactionHash(String value) {
        return new StarknetTransactionHash(value);
    }

    static StarknetAccountAddress accountAddress(String value) {
        return new StarknetAccountAddress(value);
    }

    static StarknetContractAddress contractAddress(String address) {
        return new StarknetContractAddress(address);
    }
}
