package onlydust.com.marketplace.api.infura.adapters;

import com.swmansion.starknet.data.types.Felt;
import com.swmansion.starknet.provider.Provider;
import com.swmansion.starknet.provider.rpc.JsonRpcProvider;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransaction;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

public class StarknetTransactionStorageAdapter implements BlockchainTransactionStoragePort<StarknetTransaction, StarknetTransaction.Hash> {
    Provider provider;

    public StarknetTransactionStorageAdapter(final Web3Client.Properties properties) {
        provider = new JsonRpcProvider(properties.uri());
    }

    @Override
    public Optional<StarknetTransaction> get(StarknetTransaction.Hash reference) {
        final var receipt = provider.getTransactionReceipt(Felt.fromHex(reference.toString())).send();

        return Optional.ofNullable(receipt.getBlockHash())
                .map(blockHash -> provider.getBlockWithTxHashes(blockHash).send())
                .map(block -> new StarknetTransaction(
                        StarkNet.transactionHash(receipt.getHash().hexString()),
                        Instant.ofEpochSecond(block.getTimestamp()).atZone(ZoneOffset.UTC))
                );
    }
}
