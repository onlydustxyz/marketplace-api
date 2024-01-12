package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Storage;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CmcQuoteServiceAdapterTest {
    private final CmcClient.Properties properties = new CmcClient.Properties("https://pro-api.coinmarketcap.com", "<API_KEY>");
    private static final Map<Currency.Id, Integer> CURRENCY_IDS = new HashMap<>();
    private static final Currency.Id USD_ID = Currency.Id.random();
    private static final Currency.Id ETH_ID = Currency.Id.random();

    static {
        CURRENCY_IDS.put(USD_ID, 2781);
        CURRENCY_IDS.put(ETH_ID, 1027);
    }

    private final CmcClient client = new CmcClient(properties);
    private final CmcQuoteServiceAdapter.Properties adapterProperties = new CmcQuoteServiceAdapter.Properties(CURRENCY_IDS);
    private final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    private final ERC20Storage erc20Storage = mock(ERC20Storage.class);
    private final CmcQuoteServiceAdapter adapter = new CmcQuoteServiceAdapter(client, currencyStorage, erc20Storage, adapterProperties);

    private final static ERC20 USDC_ERC20 = new ERC20(Ethereum.contractAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"), "USD Coin", "USDC", 6, BigInteger.TEN);
    private final static ERC20 LORDS_ERC20 = new ERC20(Ethereum.contractAddress("0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0"), "Lords", "LORDS", 18, BigInteger.TEN);
    private final static Currency USDC = Currency.of(USDC_ERC20);
    private final static Currency LORDS = Currency.of(LORDS_ERC20);

    //    @Test
    void should_return_single_quote_from_erc20() {
        // Given
        final var currencyId = Currency.Id.random();

        // When
        final var quote = adapter.currentPrice(currencyId, USDC_ERC20, USD_ID).orElseThrow();

        // Then
        assertThat(quote.price()).isGreaterThan(BigDecimal.valueOf(0.95));
        assertThat(quote.price()).isLessThan(BigDecimal.valueOf(1.05));
        assertThat(quote.currencyId()).isEqualTo(currencyId);
        assertThat(quote.base()).isEqualTo(USD_ID);
    }

    //    @Test
    void should_return_multiple_quotes_from_ids() {
        // Given
        when(erc20Storage.all()).thenReturn(List.of(USDC_ERC20, LORDS_ERC20));
        when(currencyStorage.findByCode(USDC.code())).thenReturn(Optional.of(USDC));
        when(currencyStorage.findByCode(LORDS.code())).thenReturn(Optional.of(LORDS));

        // When
        final var quotes = adapter.currentPrice(List.of(USDC.id(), ETH_ID, LORDS.id()), USD_ID);

        // Then
        assertThat(quotes).hasSize(3);

        assertThat(quotes.get(0).orElseThrow().price()).isGreaterThan(BigDecimal.valueOf(0.95));
        assertThat(quotes.get(0).orElseThrow().price()).isLessThan(BigDecimal.valueOf(1.05));
        assertThat(quotes.get(0).orElseThrow().currencyId()).isEqualTo(USDC.id());
        assertThat(quotes.get(0).orElseThrow().base()).isEqualTo(USD_ID);

        assertThat(quotes.get(1).orElseThrow().price()).isGreaterThan(BigDecimal.valueOf(2500));
        assertThat(quotes.get(1).orElseThrow().price()).isLessThan(BigDecimal.valueOf(3000));
        assertThat(quotes.get(1).orElseThrow().currencyId()).isEqualTo(ETH_ID);
        assertThat(quotes.get(1).orElseThrow().base()).isEqualTo(USD_ID);

        assertThat(quotes.get(2).orElseThrow().price()).isGreaterThan(BigDecimal.valueOf(0.40));
        assertThat(quotes.get(2).orElseThrow().price()).isLessThan(BigDecimal.valueOf(0.60));
        assertThat(quotes.get(2).orElseThrow().currencyId()).isEqualTo(LORDS.id());
        assertThat(quotes.get(2).orElseThrow().base()).isEqualTo(USD_ID);
    }
}