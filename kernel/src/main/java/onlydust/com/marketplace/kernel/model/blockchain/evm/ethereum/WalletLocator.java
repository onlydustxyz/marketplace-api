package onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;

import java.util.Optional;

@EqualsAndHashCode
public class WalletLocator {
    private final EvmAccountAddress address;
    private final Name ens;

    public WalletLocator(@NonNull EvmAccountAddress address) {
        this.address = address;
        this.ens = null;
    }

    public WalletLocator(@NonNull Name ens) {
        this.address = null;
        this.ens = ens;
    }

    public String asString() {
        if (address == null) {
            assert ens != null;
            return ens.asString();
        } else {
            return address.toString();
        }
    }

    public Optional<EvmAccountAddress> accountAddress() {
        return Optional.ofNullable(address);
    }

    public Optional<Name> ens() {
        return Optional.ofNullable(ens);
    }
}
