package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CmcQuoteServiceAdapterTest {
    private static final Currency USD = Currency.fiat("US Dollar", Currency.Code.of("USD"), 2);
    private static final Currency ETH = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);

    private final CmcClient.Properties properties = new CmcClient.Properties(
            "https://pro-api.coinmarketcap.com",
            "<API_KEY>");

    private final CmcClient client = new CmcClient(properties);
    private final CmcQuoteServiceAdapter adapter = new CmcQuoteServiceAdapter(client);

    private final static ERC20 USDC_ERC20 = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"), "USD Coin"
            , "USDC", 6, BigInteger.TEN);
    private final static ERC20 LORDS_ERC20 = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0"), "Lords",
            "LORDS", 18, BigInteger.TEN);
    private final static Currency USDC = Currency.of(USDC_ERC20);
    private final static Currency LORDS = Currency.of(LORDS_ERC20);

    //    @Test
    void should_return_multiple_quotes_from_ids() {
        // When
        final var quotes = adapter.currentPrice(Set.of(USDC, ETH, LORDS), Set.of(USD));

        // Then
        assertThat(quotes).hasSize(3);

        assertThat(quotes.get(0).price()).isGreaterThan(BigDecimal.valueOf(0.95));
        assertThat(quotes.get(0).price()).isLessThan(BigDecimal.valueOf(1.05));
        assertThat(quotes.get(0).currencyId()).isEqualTo(USDC.id());
        assertThat(quotes.get(0).base()).isEqualTo(USD.id());

        assertThat(quotes.get(1).price()).isGreaterThan(BigDecimal.valueOf(2500));
        assertThat(quotes.get(1).price()).isLessThan(BigDecimal.valueOf(3000));
        assertThat(quotes.get(1).currencyId()).isEqualTo(ETH.id());
        assertThat(quotes.get(1).base()).isEqualTo(USD.id());

        assertThat(quotes.get(2).price()).isGreaterThan(BigDecimal.valueOf(0.40));
        assertThat(quotes.get(2).price()).isLessThan(BigDecimal.valueOf(0.60));
        assertThat(quotes.get(2).currencyId()).isEqualTo(LORDS.id());
        assertThat(quotes.get(2).base()).isEqualTo(USD.id());
    }
}