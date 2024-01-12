package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.util.List;

public interface ERC20Storage {
    void save(ERC20 erc20);

    Boolean exists(Blockchain blockchain, ContractAddress address);

    List<ERC20> all();
}
