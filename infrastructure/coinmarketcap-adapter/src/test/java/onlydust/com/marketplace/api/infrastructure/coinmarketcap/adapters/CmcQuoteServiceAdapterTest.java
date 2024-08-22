package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import com.github.tomakehurst.wiremock.WireMockServer;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class CmcQuoteServiceAdapterTest {
    private static final Currency USD = Currency.fiat("US Dollar", Currency.Code.of("USD"), 2)
            .withMetadata(new Currency.Metadata(2781, "US Dollar", null, null));
    private static final Currency ETH = Currency.crypto("Ether", Currency.Code.of("ETH"), 18)
            .withMetadata(new Currency.Metadata(1027, "Ether", null, null));

    private CmcClient.Properties properties;

    private CmcQuoteServiceAdapter adapter;

    private final static ERC20 USDC_ERC20_ETH = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"), "USD " +
                                                                                                                                                       "Coin"
            , "USDC", 6, BigInteger.TEN);

    private final static ERC20 USDC_ERC20_STARKNET = new ERC20(Blockchain.STARKNET, StarkNet.contractAddress(
            "0x053c91253bc9682c04929ca02ed00b3e423f6710d2ee7e0d5ebb06f3ecf368a8"), "USD Coin", "USDC", 6, BigInteger.TEN);
    private final static ERC20 LORDS_ERC20 = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0"), "Lords",
            "LORDS", 18, BigInteger.TEN);
    private final static Currency USDC = Currency.of(USDC_ERC20_ETH).withERC20(USDC_ERC20_STARKNET);
    private final static Currency LORDS = Currency.of(LORDS_ERC20);

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();

        properties = new CmcClient.Properties(wireMockServer.baseUrl(), "CMC_API_KEY");
        adapter = new CmcQuoteServiceAdapter(new CmcClient(properties));
    }

    @Test
    void should_handle_bad_request() {
        // Given
        final var currency = Currency.of(USDC_ERC20_STARKNET).withERC20(USDC_ERC20_ETH).withMetadata(new Currency.Metadata(3408, "USDC", null, null));

        wireMockServer.stubFor(get(urlEqualTo("/v2/cryptocurrency/info?aux=logo,description&address=%s".formatted(USDC_ERC20_STARKNET.getAddress())))
                .willReturn(aResponse().withStatus(400).withBody("""
                        {
                          "status": { "error_code": 400 }
                        }
                        """)));

        wireMockServer.stubFor(get(urlEqualTo("/v2/cryptocurrency/info?aux=logo,description&address=%s".formatted(USDC_ERC20_ETH.getAddress())))
                .willReturn(aResponse().withStatus(200).withBody("""
                        {
                          "status": { "error_code": 0 },
                          "data": {
                            "3408": { "id": 3408, "name": "USDC", "symbol": "USDC" }
                          }
                        }
                        """)));

        wireMockServer.stubFor(get(urlEqualTo("/v1/fiat/map?limit=5000"))
                .willReturn(aResponse().withStatus(200).withBody("""
                        {
                            "status": { "error_code": 0 },
                            "data": [{ "id": 2781, "name": "US Dollar", "symbol": "USD" }]
                        }
                        """)));


        wireMockServer.stubFor(get(urlEqualTo("/v2/cryptocurrency/quotes/latest?id=3408&convert_id=2781"))
                .willReturn(aResponse().withStatus(200).withBody("""
                        {
                           "status": { "error_code": 0 },
                           "data": {
                             "3408": {
                               "id": 3408,
                               "quote": {
                                 "2781": { "price": 1.0, "last_updated": "2024-02-07T15:04:00.000Z" }
                               }
                             }
                           }
                         }
                        """)));

        final var quotes = adapter.currentPrice(Set.of(currency), Set.of(USD));

        assertThat(quotes).hasSize(1);
        assertThat(quotes.get(0).price()).isEqualByComparingTo(BigDecimal.ONE);
    }

    //    @Test
    void should_return_multiple_quotes_from_ids() {
        // When
        final var quotes = adapter.currentPrice(Set.of(USDC, ETH, LORDS), Set.of(USD));

        // Then
        assertThat(quotes).hasSize(3);

        assertThat(quotes.get(0).price()).isGreaterThan(BigDecimal.valueOf(0.95));
        assertThat(quotes.get(0).price()).isLessThan(BigDecimal.valueOf(1.05));
        assertThat(quotes.get(0).base()).isEqualTo(USDC.id());
        assertThat(quotes.get(0).target()).isEqualTo(USD.id());

        assertThat(quotes.get(1).price()).isGreaterThan(BigDecimal.valueOf(2500));
        assertThat(quotes.get(1).price()).isLessThan(BigDecimal.valueOf(3000));
        assertThat(quotes.get(1).base()).isEqualTo(ETH.id());
        assertThat(quotes.get(1).target()).isEqualTo(USD.id());

        assertThat(quotes.get(2).price()).isGreaterThan(BigDecimal.valueOf(0.40));
        assertThat(quotes.get(2).price()).isLessThan(BigDecimal.valueOf(0.60));
        assertThat(quotes.get(2).base()).isEqualTo(LORDS.id());
        assertThat(quotes.get(2).target()).isEqualTo(USD.id());
    }
}