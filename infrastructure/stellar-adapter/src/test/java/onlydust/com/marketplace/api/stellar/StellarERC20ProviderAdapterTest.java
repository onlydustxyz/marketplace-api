package onlydust.com.marketplace.api.stellar;

import onlydust.com.marketplace.kernel.model.blockchain.Stellar;

class StellarERC20ProviderAdapterTest {
    final StellarClient.Properties properties = new StellarClient.Properties("https://soroban-rpc.mainnet.stellar.gateway.fm",
            "GAIYZIEWGAEYIVMX5TMSD43HROWXX5WG35KTL6467P52S477IQQJIUEL"
    );
    final StellarClient client = new StellarClient(properties);
    final StellarERC20ProviderAdapter adapter = new StellarERC20ProviderAdapter(client);

    //    @Test
    void should_get_erc_20() {
        final var erc20 = adapter.get(Stellar.contractAddress("CCW67TSZV3SSS2HXMBQ5JFGCKJNXKZM7UQUWUZPUTHXSTZLEO7SJMI75")); // prod
        System.out.println(erc20);
    }
}
