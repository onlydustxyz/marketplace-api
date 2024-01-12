package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CmcQuoteServiceAdapterTest {
    private final CmcClient.Properties properties = new CmcClient.Properties("https://pro-api.coinmarketcap.com", "<API_KEY>");
    private static final Map<Currency.Id, Integer> CURRENCY_IDS = new HashMap<>();
    private static final Currency.Id USD_ID = Currency.Id.random();
    private static final Currency.Id USDC_ID = Currency.Id.random();
    private static final Currency.Id LORDS_ID = Currency.Id.random();
    private static final Currency.Id ETH_ID = Currency.Id.random();

    static {
        CURRENCY_IDS.put(USD_ID, 2781);
        CURRENCY_IDS.put(USDC_ID, 3408);
        CURRENCY_IDS.put(LORDS_ID, 17445);
        CURRENCY_IDS.put(ETH_ID, 1027);
    }

    private final CmcClient client = new CmcClient(properties);
    private final CmcQuoteServiceAdapter adapter = new CmcQuoteServiceAdapter(client, CURRENCY_IDS);

    private final static ERC20 USDC = new ERC20(Ethereum.contractAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"), "USD Coin", "USDC", 6, BigInteger.TEN);

    //    @Test
    void should_return_single_quote_from_erc20() {
        final var currencyId = Currency.Id.random();
        final var quote = adapter.currentPrice(currencyId, USDC, USD_ID).orElseThrow();
        assertThat(quote.price()).isGreaterThan(BigDecimal.valueOf(0.95));
        assertThat(quote.price()).isLessThan(BigDecimal.valueOf(1.05));
        assertThat(quote.currencyId()).isEqualTo(currencyId);
        assertThat(quote.base()).isEqualTo(USD_ID);
    }

    //    @Test
    void should_return_multiple_quotes_from_ids() {
        final var quotes = adapter.currentPrice(List.of(USDC_ID, ETH_ID, LORDS_ID), USD_ID);

        assertThat(quotes).hasSize(3);

        assertThat(quotes.get(0).orElseThrow().price()).isGreaterThan(BigDecimal.valueOf(0.95));
        assertThat(quotes.get(0).orElseThrow().price()).isLessThan(BigDecimal.valueOf(1.05));
        assertThat(quotes.get(0).orElseThrow().currencyId()).isEqualTo(USDC_ID);
        assertThat(quotes.get(0).orElseThrow().base()).isEqualTo(USD_ID);

        assertThat(quotes.get(1).orElseThrow().price()).isGreaterThan(BigDecimal.valueOf(2500));
        assertThat(quotes.get(1).orElseThrow().price()).isLessThan(BigDecimal.valueOf(3000));
        assertThat(quotes.get(1).orElseThrow().currencyId()).isEqualTo(ETH_ID);
        assertThat(quotes.get(1).orElseThrow().base()).isEqualTo(USD_ID);

        assertThat(quotes.get(2).orElseThrow().price()).isGreaterThan(BigDecimal.valueOf(0.40));
        assertThat(quotes.get(2).orElseThrow().price()).isLessThan(BigDecimal.valueOf(0.60));
        assertThat(quotes.get(2).orElseThrow().currencyId()).isEqualTo(LORDS_ID);
        assertThat(quotes.get(2).orElseThrow().base()).isEqualTo(USD_ID);
    }
}