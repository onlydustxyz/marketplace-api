package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.kernel.model.blockchain.*;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransaction;

import java.util.Optional;

import static java.util.function.Function.identity;

@AllArgsConstructor
public class BlockchainService implements BlockchainFacadePort {
    private final BlockchainTransactionStoragePort<EvmTransaction, EvmTransaction.Hash> ethereumTransactionStoragePort;
    private final BlockchainTransactionStoragePort<EvmTransaction, EvmTransaction.Hash> optimismTransactionStoragePort;
    private final BlockchainTransactionStoragePort<AptosTransaction, AptosTransaction.Hash> aptosTransactionStoragePort;
    private final BlockchainTransactionStoragePort<StarknetTransaction, StarknetTransaction.Hash> starknetTransactionStoragePort;
    private final BlockchainTransactionStoragePort<StellarTransaction, StellarTransaction.Hash> stellarTransactionStoragePort;

    @Override
    public Optional<Blockchain.Transaction> getTransaction(Blockchain blockchain, String reference) {
        return (switch (blockchain) {
            case ETHEREUM -> ethereumTransactionStoragePort.get(Ethereum.transactionHash(reference)).map(identity());
            case OPTIMISM -> optimismTransactionStoragePort.get(Optimism.transactionHash(reference)).map(identity());
            case APTOS -> aptosTransactionStoragePort.get(Aptos.transactionHash(reference)).map(identity());
            case STARKNET -> starknetTransactionStoragePort.get(StarkNet.transactionHash(reference)).map(identity());
            case STELLAR -> stellarTransactionStoragePort.get(Stellar.transactionHash(reference)).map(identity());
        });
    }

    @Override
    public String sanitizedTransactionReference(Blockchain blockchain, String reference) {
        return (switch (blockchain) {
            case ETHEREUM -> Ethereum.transactionHash(reference).toString();
            case OPTIMISM -> Optimism.transactionHash(reference).toString();
            case APTOS -> Aptos.transactionHash(reference).toString();
            case STARKNET -> StarkNet.transactionHash(reference).toString();
            case STELLAR -> Stellar.transactionHash(reference).toString();
        });
    }
}
