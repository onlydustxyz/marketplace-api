package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.kernel.model.blockchain.*;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransaction;

import java.time.ZonedDateTime;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class BlockchainService implements BlockchainFacadePort {
    private final BlockchainTransactionStoragePort<EvmTransaction, EvmTransaction.Hash> ethereumTransactionStoragePort;
    private final BlockchainTransactionStoragePort<EvmTransaction, EvmTransaction.Hash> optimismTransactionStoragePort;
    private final BlockchainTransactionStoragePort<AptosTransaction, AptosTransaction.Hash> aptosTransactionStoragePort;
    private final BlockchainTransactionStoragePort<StarknetTransaction, StarknetTransaction.Hash> starknetTransactionStoragePort;

    @Override
    public ZonedDateTime getTransactionTimestamp(Blockchain blockchain, String reference) {
        return (switch (blockchain) {
            case ETHEREUM -> ethereumTransactionStoragePort.get(Ethereum.transactionHash(reference)).map(EvmTransaction::timestamp);
            case OPTIMISM -> optimismTransactionStoragePort.get(Optimism.transactionHash(reference)).map(EvmTransaction::timestamp);
            case APTOS -> aptosTransactionStoragePort.get(Aptos.transactionHash(reference)).map(AptosTransaction::timestamp);
            case STARKNET -> starknetTransactionStoragePort.get(StarkNet.transactionHash(reference)).map(StarknetTransaction::timestamp);
            case STELLAR -> throw new IllegalStateException("Stellar not fully supported yet");
        }).orElseThrow(() -> notFound("Transaction %s not found on blockchain %s".formatted(reference, blockchain)));
    }
}
