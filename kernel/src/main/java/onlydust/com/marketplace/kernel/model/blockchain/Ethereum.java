package onlydust.com.marketplace.kernel.model.blockchain;


import onlydust.com.marketplace.kernel.model.blockchain.evm.AccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.TransactionHash;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.EtherScan;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Wallet;

public interface Ethereum {
    BlockExplorer<TransactionHash> BLOCK_EXPLORER = new EtherScan();

    static TransactionHash transactionHash(String value) {
        return new TransactionHash(value);
    }

    static AccountAddress accountAddress(String value) {
        return new AccountAddress(value);
    }

    static Name name(String value) {
        return new Name(value);
    }

    static Wallet wallet(String wallet) {
        return wallet.startsWith("0x") ? new Wallet(accountAddress(wallet)) : new Wallet(name(wallet));
    }

    static ContractAddress contractAddress(String address) {
        return new ContractAddress(address);
    }
}
