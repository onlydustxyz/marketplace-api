package onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infrastructure.aptosrpc.RpcClient;
import onlydust.com.marketplace.api.infrastructure.aptosrpc.RpcClient.TransactionResponse.Event;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosTransferTransaction;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class AptosTransactionStorageAdapter implements BlockchainTransactionStoragePort<AptosTransaction, AptosTransaction.Hash> {
    private final RpcClient client;

    @Override
    public Optional<AptosTransaction> get(AptosTransaction.Hash reference) {
        return client.getTransactionByHash(reference.toString())
                .map(AptosTransactionStorageAdapter::fromTransaction);
    }

    private static @NonNull AptosTransaction fromTransaction(final @NonNull RpcClient.TransactionResponse transaction) {
        if (!transaction.payload().isTransfer())
            throw badRequest("Transaction %s is not a transfer transaction".formatted(transaction.hash()));

        final var withdrawal = transaction.events().stream().filter(e -> e.type() == Event.Type.WITHDRAWAL).findFirst()
                .orElseThrow(() -> badRequest("Transaction %s does not contain a withdrawal event".formatted(transaction.hash())));

        final var deposit = transaction.events().stream().filter(e -> e.type() == Event.Type.DEPOSIT).findFirst()
                .orElseThrow(() -> badRequest("Transaction %s does not contain a deposit event".formatted(transaction.hash())));

        if (withdrawal.data().asWithdrawal().amount().compareTo(deposit.data().asDeposit().amount()) != 0)
            throw badRequest("Transaction %s withdrawal amount %s does not match deposit amount %s".formatted(transaction.hash(),
                    withdrawal.data().asWithdrawal().amount(), deposit.data().asDeposit().amount()));

        return new AptosTransferTransaction(
                Aptos.transactionHash(transaction.hash()),
                Instant.ofEpochMilli(transaction.timestamp() / 1000).atZone(ZoneOffset.UTC),
                transaction.success() ? Blockchain.Transaction.Status.CONFIRMED : AptosTransaction.Status.FAILED,
                Aptos.accountAddress(withdrawal.guid().accountAddress()),
                Aptos.accountAddress(deposit.guid().accountAddress()),
                withdrawal.data().asWithdrawal().amount(),
                Aptos.coinType(transaction.payload().typeArguments().get(0))
        );
    }
}
