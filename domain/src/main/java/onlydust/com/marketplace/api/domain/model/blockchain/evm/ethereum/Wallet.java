package onlydust.com.marketplace.api.domain.model.blockchain.evm.ethereum;

import lombok.NonNull;
import onlydust.com.marketplace.api.domain.model.blockchain.evm.AccountAddress;
import org.springframework.lang.Nullable;

import java.util.Optional;

public class Wallet {
    private final @Nullable AccountAddress address;
    private final @Nullable Name ens;

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
