package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

public class CurrencyService {
    public void addERC20Support(Blockchain blockchain, ContractAddress tokenAddress) {
        if (!blockchain.isEvmCompatible()) {
            throw OnlyDustException.badRequest("%s is not EVM compatible".formatted(blockchain));
        }
    }
}
