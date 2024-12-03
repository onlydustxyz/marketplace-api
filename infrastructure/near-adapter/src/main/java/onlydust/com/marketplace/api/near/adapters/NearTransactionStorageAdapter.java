package onlydust.com.marketplace.api.near.adapters;

import com.syntifi.near.api.common.exception.NearException;
import com.syntifi.near.api.rpc.model.transaction.TransactionStatus;
import com.syntifi.near.api.rpc.model.transaction.TransferAction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.near.NearClient;
import onlydust.com.marketplace.api.near.dto.TxExecutionStatus;
import onlydust.com.marketplace.kernel.model.blockchain.near.NearTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.near.NearTransferTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.Transaction.Status.CONFIRMED;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.Transaction.Status.FAILED;

@AllArgsConstructor
@Slf4j
public class NearTransactionStorageAdapter implements BlockchainTransactionStoragePort<NearTransaction, NearTransaction.Hash> {
    private final NearClient client;
    private static final long NANOS_PER_SECOND = 1000_000_000L;

    @Override
    public Optional<NearTransaction> get(NearTransaction.Hash reference) {
        try {
            final var transaction = client.getTransactionStatus(reference.toString(), "xx", TxExecutionStatus.NONE);
            return Optional.of(from(transaction));
        } catch (NearException e) {
            LOGGER.warn("Transaction not found: {}", reference, e);
            return Optional.empty();
        }
    }

    private NearTransaction from(TransactionStatus transactionStatus) {
        final var block = client.getBlock(transactionStatus.getTransactionOutcome().getBlockHash().getEncodedHash());
        final var transactionHash = new NearTransaction.Hash(transactionStatus.getTransaction().getHash().getEncodedHash());
        final var action = transactionStatus.getTransaction().getActions().stream()
                .filter(a -> a instanceof TransferAction)
                .map(a -> (TransferAction) a)
                .findFirst()
                .orElseThrow(() -> badRequest("Transaction %s has no transfer action".formatted(transactionHash)));

        return new NearTransferTransaction(
                transactionHash,
                fromNanos(block.getHeader().getTimeStamp()),
                transactionStatus.getStatus().getSuccessValue() != null ? CONFIRMED : FAILED,
                transactionStatus.getTransaction().getSignerId(),
                transactionStatus.getTransaction().getReceiverId(),
                new BigDecimal(action.getDeposit(), 24),
                null
        );
    }

    private ZonedDateTime fromNanos(long nanos) {
        return Instant.ofEpochSecond(Math.floorDiv(nanos, NANOS_PER_SECOND), Math.floorMod(nanos, NANOS_PER_SECOND)).atZone(ZoneOffset.UTC);
    }
}
