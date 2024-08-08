package onlydust.com.marketplace.api.stellar.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.api.stellar.SorobanClient;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;
import org.stellar.sdk.scval.Scv;

import java.math.BigInteger;
import java.util.Optional;

@AllArgsConstructor
public class StellarERC20ProviderAdapter implements ERC20Provider {
    private final SorobanClient client;

    @Override
    public Optional<ERC20> get(@NonNull Hash address) {
        final var name = new String(Scv.fromString(client.call(address.toString(), "name")));
        final var symbol = new String(Scv.fromString(client.call(address.toString(), "symbol")));
        final var decimals = (Long) Scv.fromUint32(client.call(address.toString(), "decimals"));

        return Optional.of(new ERC20(Blockchain.STELLAR, address, name, symbol, decimals.intValue(), BigInteger.ZERO));
    }
}

