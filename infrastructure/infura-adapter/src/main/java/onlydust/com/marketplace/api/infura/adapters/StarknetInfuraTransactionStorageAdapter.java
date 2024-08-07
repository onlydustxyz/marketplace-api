package onlydust.com.marketplace.api.infura.adapters;

import com.swmansion.starknet.data.types.Felt;
import com.swmansion.starknet.data.types.StarknetChainId;
import com.swmansion.starknet.data.types.transactions.ProcessedInvokeTransactionReceipt;
import com.swmansion.starknet.provider.Provider;
import com.swmansion.starknet.provider.rpc.JsonRpcProvider;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransaction;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

public class StarknetInfuraTransactionStorageAdapter implements BlockchainTransactionStoragePort<StarknetTransaction, StarknetTransaction.Hash> {
    Provider provider;

    public StarknetInfuraTransactionStorageAdapter(final InfuraClient.Properties properties) {
        provider = new JsonRpcProvider("%s/%s".formatted(properties.getBaseUri(), properties.getApiKey()), StarknetChainId.MAINNET);
    }

    @Override
    public Optional<StarknetTransaction> get(StarknetTransaction.Hash reference) {
        final var receipt = provider.getTransactionReceipt(Felt.fromHex(reference.toString())).send();

        if (receipt instanceof ProcessedInvokeTransactionReceipt invokeReceipt) {
            final var block = provider.getBlockWithTxHashes(invokeReceipt.getBlockHash()).send();
            return Optional.of(new StarknetTransaction(
                    StarkNet.transactionHash(receipt.getHash().hexString()),
                    Instant.ofEpochSecond(block.getTimestamp()).atZone(ZoneOffset.UTC))
            );
        }

        return Optional.empty();
    }
}
