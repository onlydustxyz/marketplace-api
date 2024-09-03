package onlydust.com.marketplace.api.stellar;

import onlydust.com.marketplace.api.stellar.adapters.StellarTransactionStorageAdapter;
import onlydust.com.marketplace.kernel.model.blockchain.Stellar;

class StellarTransactionStorageAdapterTest {
    final SorobanClient soroban = new SorobanClient(new SorobanClient.Properties("https://soroban-rpc.mainnet.stellar.gateway.fm",
            "GAIYZIEWGAEYIVMX5TMSD43HROWXX5WG35KTL6467P52S477IQQJIUEL"));
    final HorizonClient horizon = new HorizonClient(new HorizonClient.Properties("https://stellar.public-rpc.com/http/stellar_horizon"));
    final StellarTransactionStorageAdapter adapter = new StellarTransactionStorageAdapter(soroban, horizon);

    //@Test
    void get_transaction_info() {
        final var transaction = adapter.get(Stellar.transactionHash("97157d6c947af69ea379edee2883562cb4b18a7882d366d368b595d899d82835"));
        System.out.println(transaction);
    }
}