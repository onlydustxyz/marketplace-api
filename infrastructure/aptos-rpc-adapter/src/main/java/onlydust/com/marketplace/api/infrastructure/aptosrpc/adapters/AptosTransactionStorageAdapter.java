package onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infrastructure.aptosrpc.RpcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosTransaction;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

@AllArgsConstructor
public class AptosTransactionStorageAdapter implements BlockchainTransactionStoragePort<AptosTransaction, AptosTransaction.Hash> {
    private final RpcClient client;

    @Override
    public Optional<AptosTransaction> get(AptosTransaction.Hash reference) {
        return client.getTransactionByHash(reference.toString())
                .map(t -> new AptosTransaction(Aptos.transactionHash(t.hash()), Instant.ofEpochMilli(t.timestamp() / 1000).atZone(ZoneOffset.UTC)));
    }
}
