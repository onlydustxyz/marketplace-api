package onlydust.com.marketplace.api.infura.adapters;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
public class InfuraEvmTransactionStorageAdapter extends InfuraClient implements BlockchainTransactionStoragePort<EvmTransaction, EvmTransaction.Hash> {
    public InfuraEvmTransactionStorageAdapter(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<EvmTransaction> get(EvmTransaction.Hash reference) {
        return getTransactionByHash(reference.toString()).getTransaction()
                .map(tx -> {
                    final var block = getBlockByHash(tx.getBlockHash(), false).getBlock();
                    return new EvmTransaction(
                            Ethereum.transactionHash(tx.getHash()),
                            Instant.ofEpochSecond(block.getTimestamp().longValue()).atZone(ZoneOffset.UTC));
                });
    }
}
