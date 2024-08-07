package onlydust.com.marketplace.api.stellar;

import onlydust.com.marketplace.kernel.model.blockchain.Stellar;

class StellarTransactionStorageAdapterTest {
    final StellarClient.Properties properties = new StellarClient.Properties("https://soroban-rpc.mainnet.stellar.gateway.fm", "");
    final StellarClient client = new StellarClient(properties);
    final StellarTransactionStorageAdapter adapter = new StellarTransactionStorageAdapter(client);

    //    @Test
    void get_transaction_info() {
        final var transaction = adapter.get(Stellar.transactionHash("1b7ead3cae662c93c311ec21965834baabdc7b253c3c8ba5d4aaab68bab3f955"));
        System.out.println(transaction);
    }
}