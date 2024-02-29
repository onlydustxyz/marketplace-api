package onlydust.com.marketplace.kernel.model.blockchain;


import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmContractAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransactionHash;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.EtherScan;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Wallet;

public interface Ethereum {
    BlockExplorer<EvmTransactionHash> BLOCK_EXPLORER = new EtherScan();

    static EvmTransactionHash transactionHash(String value) {
        return new EvmTransactionHash(value);
    }

    static EvmAccountAddress accountAddress(String value) {
        return new EvmAccountAddress(value);
    }

    static Name name(String value) {
        return new Name(value);
    }

    static Wallet wallet(String wallet) {
        // TODO implement proper ENS check with Infura
        try {
            return new Wallet(accountAddress(wallet));
        } catch (OnlyDustException e) {
            return new Wallet(name(wallet));
        }
    }

    static EvmContractAddress contractAddress(String address) {
        return new EvmContractAddress(address);
    }
}
