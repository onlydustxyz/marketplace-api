package onlydust.com.marketplace.kernel.model.blockchain.evm;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

@EqualsAndHashCode(callSuper = true)
public class EvmTransactionHash extends Hash {
    private static final int MAX_BYTE_COUNT = 32;

    public EvmTransactionHash(final @NonNull String address) {
        super(MAX_BYTE_COUNT, address);
    }
}
