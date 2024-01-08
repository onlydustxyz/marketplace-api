package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

public interface ERC20Provider {
    ERC20 get(ContractAddress address);
}
