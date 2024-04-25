package onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.api.infrastructure.aptosrpc.RpcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

import java.math.BigInteger;
import java.util.Optional;

@AllArgsConstructor
public class AptosERC20ProviderAdapter implements ERC20Provider {
    private final RpcClient client;

    @Override
    public Optional<ERC20> get(@NonNull Hash hash) {
        final var coinType = Aptos.coinType(hash.toString());
        return client.getAccountResource(coinType.contractAddress(), coinType.resourceType())
                .map(resource -> new ERC20(
                        Blockchain.APTOS,
                        coinType,
                        resource.data().name(),
                        resource.data().symbol(),
                        resource.data().decimals(),
                        resource.data().supply().vec().stream().findFirst()
                                .flatMap(d -> d.integer().vec().stream().findFirst())
                                .map(RpcClient.TransactionResourceResponse.Integer.Data::value)
                                .orElse(BigInteger.ZERO)
                ));
    }
}
