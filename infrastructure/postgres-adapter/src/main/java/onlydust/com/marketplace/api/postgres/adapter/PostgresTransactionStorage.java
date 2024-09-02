package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.out.TransactionStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.repository.TransferTransactionRepository;

@AllArgsConstructor
public class PostgresTransactionStorage implements TransactionStoragePort {
    private final TransferTransactionRepository transferTransactionRepository;

    @Override
    public boolean exists(String transactionReference) {
        return transferTransactionRepository.existsByReference(transactionReference);
    }
}
