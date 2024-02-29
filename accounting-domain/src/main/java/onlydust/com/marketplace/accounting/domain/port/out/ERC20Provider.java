package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

import java.util.Optional;

public interface ERC20Provider {
    Optional<ERC20> get(@NonNull final Hash address);
}
