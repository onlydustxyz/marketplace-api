package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.util.Optional;

public interface ERC20Provider {
    Optional<ERC20> get(ContractAddress address);
}
