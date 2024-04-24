package onlydust.com.marketplace.kernel.model.blockchain.aptos;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

@EqualsAndHashCode(callSuper = true)
public class AptosCoinType extends Hash {
    private static final int MAX_BYTE_COUNT = 32;

    @NonNull String coinType;

    public AptosCoinType(final @NonNull String coinType) {
        super(MAX_BYTE_COUNT, coinType.split(":")[0]);
        this.coinType = coinType;
    }

    public String contractAddress() {
        return super.toString();
    }

    @Override
    public String toString() {
        return coinType;
    }

    public String resourceType() {
        return "0x1::coin::CoinInfo<%s>".formatted(coinType);
    }
}
