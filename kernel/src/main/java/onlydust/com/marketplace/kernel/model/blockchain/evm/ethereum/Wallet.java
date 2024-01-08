package onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.blockchain.evm.AccountAddress;

import java.util.Optional;

@EqualsAndHashCode
public class Wallet {
    private final AccountAddress address;
    private final Name ens;

    public Wallet(@NonNull AccountAddress address) {
        this.address = address;
        this.ens = null;
    }

    public Wallet(@NonNull Name ens) {
        this.address = null;
        this.ens = ens;
    }

    public String asString() {
        if (address == null) {
            assert ens != null;
            return ens.asString();
        } else {
            return address.asString();
        }
    }

    public Optional<AccountAddress> accountAddress() {
        return Optional.ofNullable(address);
    }

    public Optional<Name> ens() {
        return Optional.ofNullable(ens);
    }
}
