package onlydust.com.marketplace.kernel.model.blockchain.evm;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.ZonedDateTime;

public record EvmTransaction(Hash hash, ZonedDateTime timestamp) {

    @EqualsAndHashCode(callSuper = true)
    public static class Hash extends onlydust.com.marketplace.kernel.model.blockchain.Hash {
        private static final int MAX_BYTE_COUNT = 32;

        public Hash(final @NonNull String address) {
            super(MAX_BYTE_COUNT, address);
        }
    }
}
