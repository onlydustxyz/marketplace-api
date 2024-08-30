package onlydust.com.marketplace.api.infura.adapters;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransferTransaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.exceptions.ContractCallException;
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
    public Web3EvmTransactionStorageAdapter(Properties properties) {
        super(properties);
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
        return tryFromERC20(block, transaction, receipt)
                .orElseGet(() -> fromNativeTransaction(block, transaction, receipt));
    }

    private EvmTransaction fromNativeTransaction(final @NonNull EthBlock.Block block,
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

    private Optional<EvmTransaction> tryFromERC20(final @NonNull EthBlock.Block block,
                                                  final @NonNull Transaction transaction,
                                                  final @NonNull TransactionReceipt receipt) {
        try {
            final var erc20 = ERC20Contract.load(transaction.getTo(), web3j, credentials, gasPriceProvider);
            final var decimals = erc20.decimals().send();

            return erc20.getTransferEvents(receipt).stream()
                    .map(l -> new EvmTransferTransaction(
                            blockchain(),
                            Ethereum.transactionHash(transaction.getHash()),
                            Instant.ofEpochSecond(block.getTimestamp().longValue()).atZone(ZoneOffset.UTC),
                            receipt.isStatusOK() ? CONFIRMED : FAILED,
                            Ethereum.accountAddress(l._from),
                            Ethereum.accountAddress(l._to),
                            BigDecimal.valueOf(l._value.longValue(), decimals.intValue()),
                            Ethereum.contractAddress(transaction.getTo())))
                    .findFirst()
                    .map(identity());
        } catch (ContractCallException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw internalServerError("Unable to fetch ERC20 decimals at address %s".formatted(transaction.getTo()), e);
        }
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
