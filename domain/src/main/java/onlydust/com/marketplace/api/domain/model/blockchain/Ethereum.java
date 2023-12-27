package onlydust.com.marketplace.api.domain.model.blockchain;


import onlydust.com.marketplace.api.domain.model.blockchain.evm.*;
import onlydust.com.marketplace.api.domain.model.blockchain.evm.ethereum.*;

public interface Ethereum {
    BlockExplorer<TransactionHash> BLOCK_EXPLORER = new EtherScan();

    static TransactionHash transactionHash(String value) {
        return new TransactionHash(value);
    }

    static AccountAddress accountAddress(String value) {
        return new AccountAddress(value);
    }

    static Wallet wallet(String wallet) {
        return wallet.startsWith("0x") ? new Wallet(accountAddress(wallet)) : new Wallet(wallet);
    }
}
