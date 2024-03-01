package onlydust.com.marketplace.kernel.model.blockchain.starknet;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

@EqualsAndHashCode(callSuper = true)
public class StarknetContractAddress extends Hash {
    private static final int MAX_BYTE_COUNT = 32;

    public StarknetContractAddress(final @NonNull String address) {
        super(MAX_BYTE_COUNT, address);
    }
}
