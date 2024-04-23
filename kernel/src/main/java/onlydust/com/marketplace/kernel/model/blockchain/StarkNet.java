package onlydust.com.marketplace.kernel.model.blockchain;

import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarkScan;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetContractAddress;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransaction;

public interface StarkNet {
    BlockExplorer<StarknetTransaction.Hash> BLOCK_EXPLORER = new StarkScan();

    static StarknetTransaction.Hash transactionHash(String value) {
        return new StarknetTransaction.Hash(value);
    }

    static StarknetAccountAddress accountAddress(String value) {
        return new StarknetAccountAddress(value);
    }

    static StarknetContractAddress contractAddress(String address) {
        return new StarknetContractAddress(address);
    }
}
