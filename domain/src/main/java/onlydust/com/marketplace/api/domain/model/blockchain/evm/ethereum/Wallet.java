package onlydust.com.marketplace.api.domain.model.blockchain.evm.ethereum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.domain.model.blockchain.Ethereum;
import onlydust.com.marketplace.api.domain.model.blockchain.evm.AccountAddress;
import org.springframework.lang.Nullable;

import java.util.Optional;

public class Wallet {
    private final @Nullable AccountAddress address;
    private final @Nullable String ens;

    public Wallet(@NonNull AccountAddress address) {
        this.address = address;
        this.ens = null;
    }

    public Wallet(@NonNull String ens) {
        this.address = null;
        this.ens = ens;
    }

    public String asString() {
        return address == null ? ens : address.asString();
    }

    public Optional<AccountAddress> accountAddress() {
        return Optional.ofNullable(address);
    }

    public Optional<String> ens() {
        return Optional.ofNullable(ens);
    }
}
