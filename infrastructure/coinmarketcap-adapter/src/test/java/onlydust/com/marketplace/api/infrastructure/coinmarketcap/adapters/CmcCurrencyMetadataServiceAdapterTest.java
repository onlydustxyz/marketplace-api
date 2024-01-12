package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;

import java.math.BigInteger;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class CmcCurrencyMetadataServiceAdapterTest {
    private final CmcClient.Properties properties = new CmcClient.Properties(
            "https://pro-api.coinmarketcap.com",
            "<API_KEY>");
    private final CmcClient client = new CmcClient(properties);
    private final CmcCurrencyMetadataServiceAdapter adapter = new CmcCurrencyMetadataServiceAdapter(client);

    private final static ERC20 USDC = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"), "USD Coin",
            "USDC", 6, BigInteger.TEN);

    //    @Test
    void should_fetch_erc20_metadata() {
        final var metadata = adapter.get(USDC).orElseThrow();
        assertThat(metadata.description()).contains("USDC (USDC) is a cryptocurrency and operates on the Ethereum platform.");
        assertThat(metadata.logoUri()).isEqualTo(URI.create("https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png"));
    }
}