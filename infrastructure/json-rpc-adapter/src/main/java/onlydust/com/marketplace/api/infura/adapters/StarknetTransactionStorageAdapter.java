package onlydust.com.marketplace.api.infura.adapters;

import com.swmansion.starknet.data.types.BlockWithTransactionHashes;
import com.swmansion.starknet.data.types.Event;
import com.swmansion.starknet.data.types.Felt;
import com.swmansion.starknet.data.types.TransactionReceipt;
import com.swmansion.starknet.provider.Provider;
import com.swmansion.starknet.provider.rpc.JsonRpcProvider;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransferTransaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.Transaction.Status.*;

public class StarknetTransactionStorageAdapter implements BlockchainTransactionStoragePort<StarknetTransaction, StarknetTransaction.Hash> {
    Provider provider;

    public StarknetTransactionStorageAdapter(final Web3Client.Properties properties) {
        provider = new JsonRpcProvider(properties.getBaseUri());
    }

    @Override
    public Optional<StarknetTransaction> get(StarknetTransaction.Hash reference) {
        final var receipt = provider.getTransactionReceipt(Felt.fromHex(reference.toString())).send();

        return Optional.ofNullable(receipt.getBlockHash())
                .map(blockHash -> provider.getBlockWithTxHashes(blockHash).send())
                .flatMap(block -> receipt.getEvents().stream()
                        .map(event -> fromEvent(block, receipt, event))
                        .findFirst());
    }

    private @NonNull StarknetTransferTransaction fromEvent(final @NonNull BlockWithTransactionHashes block,
                                                           final @NonNull TransactionReceipt receipt,
                                                           final @NonNull Event event) {
        try {
            final var contractAddress = StarkNet.contractAddress(event.getAddress().hexString());
            final var erc20 = StarknetERC20ProviderAdapter.ERC20Contract.load(provider, contractAddress);
            final var decimals = erc20.decimals().get();

            return new StarknetTransferTransaction(
                    StarkNet.transactionHash(receipt.getHash().hexString()),
                    Instant.ofEpochSecond(block.getTimestamp()).atZone(ZoneOffset.UTC),
                    statusOf(receipt),
                    StarkNet.accountAddress(event.getData().get(0).hexString()),
                    StarkNet.accountAddress(event.getData().get(1).hexString()),
                    new BigDecimal(new BigInteger(event.getData().get(2).decString()), decimals.intValue()),
                    contractAddress
            );
        } catch (Exception e) {
            throw internalServerError("Unable to fetch ERC20 decimals", e);
        }
    }

    private Blockchain.Transaction.Status statusOf(TransactionReceipt receipt) {
        return switch (receipt.getFinalityStatus()) {
            case ACCEPTED_ON_L1, ACCEPTED_ON_L2 -> CONFIRMED;
            case RECEIVED -> switch (receipt.getExecutionStatus()) {
                case SUCCEEDED -> CONFIRMED;
                case REVERTED, REJECTED -> FAILED;
            };
            case NOT_RECEIVED -> PENDING;
        };
    }
}
