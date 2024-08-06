package onlydust.com.marketplace.api.stellar;

import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

import java.math.BigInteger;
import java.util.Optional;

public class StellarERC20ProviderAdapter implements ERC20Provider {
    @SneakyThrows
    @Override
    public Optional<ERC20> get(@NonNull Hash address) {
        // TODO - integrate with Soroban to get ERC20 information
        return Optional.of(new ERC20(Blockchain.STELLAR, address, "", "", 0, BigInteger.ZERO));
    }
}
