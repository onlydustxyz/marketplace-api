package onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor
public class Name {
    private final String ens;

    public String toString() {
        return ens;
    }
}
