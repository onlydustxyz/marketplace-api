package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.TransactionStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.TransferTransactionRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

@AllArgsConstructor
public class PostgresTransactionStorage implements TransactionStoragePort {
    private final TransferTransactionRepository transferTransactionRepository;

    @Override
    public boolean exists(final @NonNull Blockchain blockchain, final @NonNull String transactionReference) {
        return transferTransactionRepository.existsByBlockchainAndReference(NetworkEnumEntity.of(blockchain), transactionReference);
    }
}
