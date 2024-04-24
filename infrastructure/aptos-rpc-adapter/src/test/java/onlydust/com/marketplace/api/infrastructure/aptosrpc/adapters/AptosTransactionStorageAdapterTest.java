package onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters;

import onlydust.com.marketplace.api.infrastructure.aptosrpc.RpcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AptosTransactionStorageAdapterTest {
    final RpcClient.Properties properties = new RpcClient.Properties("https://api.mainnet.aptoslabs.com/v1");
    final RpcClient client = new RpcClient(properties);
    final AptosTransactionStorageAdapter aptosAccountValidatorAdapter = new AptosTransactionStorageAdapter(client);

    //    @Test
    void get_transaction_by_hash() {
        assertThat(aptosAccountValidatorAdapter.get(Aptos.transactionHash("0xa35864ccdb3abcb64c144da4511c66457f743ee0ddf95c1b5bbfabaf67e6ac73"))).isEmpty();
        final var transaction = aptosAccountValidatorAdapter.get(Aptos.transactionHash("0xffae983a8a8498980c4ecfd88eef5615037cad97ed1f1d7d727137421656cb2f"));
        assertThat(transaction).isPresent();
        assertThat(transaction.get().hash()).isEqualTo(Aptos.transactionHash("0xffae983a8a8498980c4ecfd88eef5615037cad97ed1f1d7d727137421656cb2f"));
        assertThat(transaction.get().timestamp()).isEqualToIgnoringNanos(ZonedDateTime.of(2024, 4, 24, 8, 42, 44, 86_000_000, ZoneOffset.UTC));
    }
}