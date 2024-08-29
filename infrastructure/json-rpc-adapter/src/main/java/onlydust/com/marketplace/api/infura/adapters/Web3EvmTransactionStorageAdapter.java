package onlydust.com.marketplace.api.infura.adapters;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransferTransaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.web3j.utils.Convert.fromWei;

@Slf4j
public class Web3EvmTransactionStorageAdapter extends Web3Client implements BlockchainTransactionStoragePort<EvmTransaction, EvmTransaction.Hash> {
    public Web3EvmTransactionStorageAdapter(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<EvmTransaction> get(EvmTransaction.Hash reference) {
        return getTransactionByHash(reference.toString()).getTransaction()
                .map(tx -> {
                    final var block = getBlockByHash(tx.getBlockHash(), false).getBlock();
                    return new EvmTransferTransaction(
                            blockchain(),
                            Ethereum.transactionHash(tx.getHash()),
                            Instant.ofEpochSecond(block.getTimestamp().longValue()).atZone(ZoneOffset.UTC),
                            Ethereum.accountAddress(tx.getFrom()),
                            Ethereum.accountAddress(tx.getTo()),
                            fromWei(BigDecimal.valueOf(tx.getValue().longValue()), Convert.Unit.ETHER));
                });
    }
}
