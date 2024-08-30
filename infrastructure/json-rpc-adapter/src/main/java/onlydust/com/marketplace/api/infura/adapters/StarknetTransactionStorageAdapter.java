package onlydust.com.marketplace.api.infura.adapters;

import com.swmansion.starknet.data.types.Felt;
import com.swmansion.starknet.data.types.TransactionReceipt;
import com.swmansion.starknet.provider.Provider;
import com.swmansion.starknet.provider.rpc.JsonRpcProvider;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransaction;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

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
                .map(block -> new StarknetTransaction(
                        StarkNet.transactionHash(receipt.getHash().hexString()),
                        Instant.ofEpochSecond(block.getTimestamp()).atZone(ZoneOffset.UTC),
                        statusOf(receipt))
                );
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
