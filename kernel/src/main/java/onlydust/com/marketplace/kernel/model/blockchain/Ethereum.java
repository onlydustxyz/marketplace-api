package onlydust.com.marketplace.kernel.model.blockchain;


import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmContractAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.EtherScan;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;

public interface Ethereum {
    BlockExplorer<EvmTransaction.Hash> BLOCK_EXPLORER = new EtherScan();

    static EvmTransaction.Hash transactionHash(String value) {
        return new EvmTransaction.Hash(value);
    }

    static EvmAccountAddress accountAddress(String value) {
        return new EvmAccountAddress(value);
    }

    static Name name(String value) {
        return new Name(value);
    }

    static WalletLocator wallet(String wallet) {
        try {
            return new WalletLocator(accountAddress(wallet));
        } catch (OnlyDustException e) {
            return new WalletLocator(name(wallet));
        }
    }

    static EvmContractAddress contractAddress(String address) {
        return new EvmContractAddress(address);
    }
}
