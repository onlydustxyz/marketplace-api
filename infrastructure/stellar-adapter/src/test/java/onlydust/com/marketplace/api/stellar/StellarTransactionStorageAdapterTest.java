package onlydust.com.marketplace.api.stellar;

import onlydust.com.marketplace.api.stellar.adapters.StellarTransactionStorageAdapter;
import onlydust.com.marketplace.kernel.model.blockchain.Stellar;
import org.junit.jupiter.api.Test;

class StellarTransactionStorageAdapterTest {
    final SorobanClient.Properties properties = new SorobanClient.Properties("https://soroban-rpc.mainnet.stellar.gateway.fm",
            "");
    final SorobanClient client = new SorobanClient(properties);
    final StellarTransactionStorageAdapter adapter = new StellarTransactionStorageAdapter(client);

    @Test
    void get_transaction_info() {
        final var transaction = adapter.get(Stellar.transactionHash("01cab5c04cf265b2995a2e5c4e961cad82d38bfb9e950ec3f6e33e5ff28500d8"));
        System.out.println(transaction);
    }
}