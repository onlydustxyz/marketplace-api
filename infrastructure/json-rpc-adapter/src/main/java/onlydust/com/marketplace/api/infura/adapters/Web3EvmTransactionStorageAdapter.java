package onlydust.com.marketplace.api.infura.adapters;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.api.infura.contracts.ERC20;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransferTransaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static java.util.function.Function.identity;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.Transaction.Status.*;
import static org.web3j.utils.Convert.fromWei;

@Slf4j
public class Web3EvmTransactionStorageAdapter extends Web3Client implements BlockchainTransactionStoragePort<EvmTransaction, EvmTransaction.Hash> {
    private final EthWeb3ERC20ProviderAdapter erc20Provider;

    public Web3EvmTransactionStorageAdapter(Properties properties, EthWeb3ERC20ProviderAdapter erc20Provider) {
        super(properties);
        this.erc20Provider = erc20Provider;
    }

    @Override
    public Optional<EvmTransaction> get(EvmTransaction.Hash reference) {
        return getTransactionByHash(reference.toString()).getTransaction()
                .map(tx -> getTransactionReceipt(tx.getHash()).getTransactionReceipt()
                        .map(receipt -> buildTransaction(tx, receipt))
                        .orElseGet(() -> buildTransaction(tx)));
    }

    private EvmTransaction buildTransaction(final @NonNull Transaction transaction, final @NonNull TransactionReceipt receipt) {
        final var block = getBlockByHash(transaction.getBlockHash(), false).getBlock();
        final var erc20 = erc20Provider.get(Ethereum.contractAddress(transaction.getTo()));
        return erc20.map(e -> buildTransaction(block, transaction, receipt, e)
                        .orElseThrow(() -> internalServerError("Unable to build ERC20 transaction")))
                .orElseGet(() -> buildTransaction(block, transaction, receipt));
    }

    private EvmTransaction buildTransaction(final @NonNull EthBlock.Block block,
                                            final @NonNull Transaction transaction,
                                            final @NonNull TransactionReceipt receipt) {
        return new EvmTransferTransaction(
                blockchain(),
                Ethereum.transactionHash(transaction.getHash()),
                Instant.ofEpochSecond(block.getTimestamp().longValue()).atZone(ZoneOffset.UTC),
                receipt.isStatusOK() ? CONFIRMED : FAILED,
                Ethereum.accountAddress(transaction.getFrom()),
                Ethereum.accountAddress(transaction.getTo()),
                fromWei(BigDecimal.valueOf(transaction.getValue().longValue()), Convert.Unit.ETHER),
                null);
    }

    private Optional<EvmTransaction> buildTransaction(final @NonNull EthBlock.Block block,
                                                      final @NonNull Transaction transaction,
                                                      final @NonNull TransactionReceipt receipt,
                                                      final @NonNull onlydust.com.marketplace.accounting.domain.model.ERC20 erc20) {
        return ERC20.getTransferEvents(receipt).stream()
                .map(l -> new EvmTransferTransaction(
                        blockchain(),
                        Ethereum.transactionHash(transaction.getHash()),
                        Instant.ofEpochSecond(block.getTimestamp().longValue()).atZone(ZoneOffset.UTC),
                        receipt.isStatusOK() ? CONFIRMED : FAILED,
                        Ethereum.accountAddress(l.from),
                        Ethereum.accountAddress(l.to),
                        BigDecimal.valueOf(l.value.longValue(), erc20.getDecimals()),
                        Ethereum.contractAddress(transaction.getTo())))
                .findFirst()
                .map(identity());
    }

    private EvmTransaction buildTransaction(final @NonNull Transaction transaction) {
        final var block = getBlockByHash(transaction.getBlockHash(), false).getBlock();

        return new EvmTransferTransaction(
                blockchain(),
                Ethereum.transactionHash(transaction.getHash()),
                Instant.ofEpochSecond(block.getTimestamp().longValue()).atZone(ZoneOffset.UTC),
                PENDING,
                Ethereum.accountAddress(transaction.getFrom()),
                Ethereum.accountAddress(transaction.getTo()),
                fromWei(BigDecimal.valueOf(transaction.getValue().longValue()), Convert.Unit.ETHER),
                null);
    }
}
