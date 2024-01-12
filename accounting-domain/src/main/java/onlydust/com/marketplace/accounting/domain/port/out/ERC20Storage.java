package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

public interface ERC20Storage {
    void save(Blockchain blockchain, ERC20 erc20);
}
