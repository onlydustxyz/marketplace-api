package onlydust.com.marketplace.accounting.domain;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.CurrencyService;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens.*;
import static onlydust.com.marketplace.accounting.domain.stubs.Quotes.*;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class CurrencyServiceTest {
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final CurrencyMetadataService currencyMetadataService = mock(CurrencyMetadataService.class);
    final ERC20Provider ethereumERC20Provider = mock(ERC20Provider.class);
    final ERC20Provider optimismERC20Provider = mock(ERC20Provider.class);
    final ERC20Provider starknetERC20Provider = mock(ERC20Provider.class);
    final ERC20ProviderFactory erc20ProviderFactory = new ERC20ProviderFactory(ethereumERC20Provider, optimismERC20Provider, starknetERC20Provider);
    final QuoteService quoteService = mock(QuoteService.class);
    final QuoteStorage quoteStorage = mock(QuoteStorage.class);
    final IsoCurrencyService isoCurrencyService = mock(IsoCurrencyService.class);
    CurrencyService currencyService;
    private final Faker faker = new Faker();
    private final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);

    @BeforeEach
    void setUp() {
        reset(currencyStorage, ethereumERC20Provider, optimismERC20Provider, starknetERC20Provider, quoteService, quoteStorage,
                isoCurrencyService, imageStoragePort);
        when(currencyStorage.findByCode(any())).thenReturn(Optional.empty());
        when(currencyStorage.findByCode(Currencies.USD.code())).thenReturn(Optional.of(Currencies.USD));
        currencyService = new CurrencyService(erc20ProviderFactory, currencyStorage, currencyMetadataService, quoteService, quoteStorage,
                isoCurrencyService, imageStoragePort);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_add_erc20_support_on_ethereum() {
        //Given
        when(ethereumERC20Provider.get(LORDS.getAddress())).thenReturn(Optional.of(LORDS));
        when(currencyMetadataService.get(LORDS)).thenReturn(Optional.of(new Currency.Metadata("LORDS", "Realms token", URI.create("https://realms.io"))));
        when(currencyStorage.all()).thenReturn(Set.of(Currencies.USD));
        when(quoteService.currentPrice(anySet(), anySet()))
                .then(i -> createQuotesFromInvocation(i, BigDecimal.valueOf(0.35)));

        // When
        final var currency = currencyService.addERC20Support(ETHEREUM, LORDS.getAddress());

        // Then
        verify(currencyStorage, times(1)).save(currency);

        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Lords");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("LORDS"));
        assertThat(currency.description()).isEqualTo(Optional.of("Realms token"));
        assertThat(currency.logoUri()).isEqualTo(Optional.of(URI.create("https://realms.io")));
        assertThat(currency.decimals()).isEqualTo(18);
        assertThat(currency.erc20()).contains(LORDS);

        final ArgumentCaptor<List<Quote>> quotes = ArgumentCaptor.forClass(List.class);
        verify(quoteStorage, times(1)).save(quotes.capture());
        assertThat(quotes.getValue()).containsExactlyInAnyOrder(
                new Quote(currency.id(), Currencies.USD.id(), BigDecimal.valueOf(0.35), TIMESTAMP),
                new Quote(currency.id(), currency.id(), BigDecimal.ONE, TIMESTAMP)
        );
    }

    @SuppressWarnings("unchecked")
    private static List<Quote> createQuotesFromInvocation(InvocationOnMock i, BigDecimal price) {
        final Set<Currency> currencies = i.getArgument(0, Set.class);
        final Set<Currency> bases = i.getArgument(1, Set.class);
        return currencies.stream().flatMap(
                currency -> bases.stream().map(
                        base -> new Quote(currency.id(), base.id(), currency.equals(base) ? BigDecimal.ONE : price, TIMESTAMP)
                )).toList();
    }

    @Test
    void should_add_erc20_support_on_optimism() {
        // Given
        when(optimismERC20Provider.get(OP.getAddress())).thenReturn(Optional.of(OP));

        // When
        final var currency = currencyService.addERC20Support(OPTIMISM, OP.getAddress());

        // Then
        verify(currencyStorage, times(1)).save(currency);

        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Optimism");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("OP"));
        assertThat(currency.description()).isEqualTo(Optional.empty());
        assertThat(currency.logoUri()).isEqualTo(Optional.empty());
        assertThat(currency.decimals()).isEqualTo(18);
        assertThat(currency.erc20()).contains(OP);
    }


    @Test
    void should_add_erc20_support_on_starknet() {
        //Given
        when(starknetERC20Provider.get(LORDS.getAddress())).thenReturn(Optional.of(LORDS));
        when(currencyMetadataService.get(LORDS)).thenReturn(Optional.of(new Currency.Metadata("LORDS", "Realms token", URI.create("https://realms.io"))));
        when(currencyStorage.all()).thenReturn(Set.of(Currencies.USD));
        when(quoteService.currentPrice(anySet(), anySet()))
                .then(i -> createQuotesFromInvocation(i, BigDecimal.valueOf(0.35)));

        // When
        final var currency = currencyService.addERC20Support(STARKNET, LORDS.getAddress());

        // Then
        verify(currencyStorage, times(1)).save(currency);

        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Lords");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("LORDS"));
        assertThat(currency.description()).isEqualTo(Optional.of("Realms token"));
        assertThat(currency.logoUri()).isEqualTo(Optional.of(URI.create("https://realms.io")));
        assertThat(currency.decimals()).isEqualTo(18);
        assertThat(currency.erc20()).contains(LORDS);

        final ArgumentCaptor<List<Quote>> quotes = ArgumentCaptor.forClass(List.class);
        verify(quoteStorage, times(1)).save(quotes.capture());

        assertThat(quotes.getValue()).containsExactlyInAnyOrder(
                new Quote(currency.id(), Currencies.USD.id(), BigDecimal.valueOf(0.35), TIMESTAMP),
                new Quote(currency.id(), currency.id(), BigDecimal.ONE, TIMESTAMP)
        );
    }

    @Test
    void should_not_add_erc20_support_on_unsupported_blockchain() {
        // When
        assertThatThrownBy(() -> currencyService.addERC20Support(APTOS, LORDS.getAddress()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("ERC20 tokens on Aptos are not supported");
    }

    @Test
    void given_a_wrong_address_should_not_add_erc20_support() {
        // Given
        final var invalidAddress = Ethereum.contractAddress("0x388C818CA8B9251b393131C08a736A67ccB19297");
        when(ethereumERC20Provider.get(LORDS.getAddress())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> currencyService.addERC20Support(ETHEREUM, invalidAddress))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Could not find a valid ERC20 contract at address 0x388C818CA8B9251b393131C08a736A67ccB19297 on Ethereum");
    }

    @Test
    void should_not_store_quote_if_not_found() {
        // Given
        when(ethereumERC20Provider.get(LORDS.getAddress())).thenReturn(Optional.of(LORDS));
        when(currencyStorage.all()).thenReturn(Set.of(Currencies.USD));
        when(quoteService.currentPrice(anySet(), anySet())).thenReturn(List.of());

        // When
        final var currency = currencyService.addERC20Support(ETHEREUM, LORDS.getAddress());

        // Then
        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Lords");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("LORDS"));
        assertThat(currency.description()).isEmpty();
        assertThat(currency.logoUri()).isEmpty();
        assertThat(currency.decimals()).isEqualTo(18);
        assertThat(currency.erc20()).contains(LORDS);

        verify(currencyStorage, times(1)).save(currency);
        verify(quoteStorage, never()).save(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_allow_multiple_tokens_on_same_currencies() {
        // Given
        when(optimismERC20Provider.get(OP_USDC.getAddress())).thenReturn(Optional.of(OP_USDC));
        when(currencyStorage.findByCode(Currency.Code.of("USDC"))).thenReturn(Optional.of(Currencies.USDC));
        when(currencyStorage.all()).thenReturn(Set.of(Currencies.USD));
        when(quoteService.currentPrice(anySet(), anySet()))
                .then(i -> createQuotesFromInvocation(i, BigDecimal.valueOf(0.35)));

        // When
        final var currency = currencyService.addERC20Support(OPTIMISM, OP_USDC.getAddress());

        // Then
        assertThat(currency).isEqualTo(Currencies.USDC);
        assertThat(currency.erc20()).containsExactlyInAnyOrder(ETH_USDC, OP_USDC);
        verify(currencyStorage, times(1)).save(currency);
        final ArgumentCaptor<List<Quote>> quotes = ArgumentCaptor.forClass(List.class);
        verify(quoteStorage, times(1)).save(quotes.capture());
        assertThat(quotes.getValue()).containsExactlyInAnyOrder(USDC_USDC, USDC_USD);
    }

    @Test
    void should_reject_duplicate_erc20() {
        // Given
        when(ethereumERC20Provider.get(LORDS.getAddress())).thenReturn(Optional.of(LORDS));
        when(currencyStorage.findByCode(Currency.Code.of("LORDS"))).thenReturn(Optional.of(Currencies.LORDS));

        // When
        assertThatThrownBy(() -> currencyService.addERC20Support(ETHEREUM, LORDS.getAddress()))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("ERC20 token at address 0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0 on Ethereum is already supported");

        // Then
        verify(currencyStorage, never()).save(any());
        verify(quoteService, never()).currentPrice(any(), any());
        verify(quoteStorage, never()).save(any());
    }

    @Test
    void should_refresh_quotes() {
        // Given
        final var currencies = Set.of(Currencies.USDC, Currencies.LORDS, Currencies.STRK, Currencies.USD);

        when(currencyStorage.all()).thenReturn(currencies);
        when(quoteService.currentPrice(currencies, currencies))
                .thenReturn(List.of(USDC_USD, LORDS_USD));

        // When
        currencyService.refreshQuotes();

        // Then
        verify(quoteStorage, times(1)).save(List.of(USDC_USD, LORDS_USD));
    }

    @Test
    void should_add_native_cryptocurrency_support() {
        // Given
        when(currencyMetadataService.get(Currencies.ETH.code())).thenReturn(Optional.of(new Currency.Metadata(Currencies.ETH.name(),
                Currencies.ETH.description().orElseThrow(), Currencies.ETH.logoUri().orElseThrow())));

        // When
        final var currency = currencyService.addNativeCryptocurrencySupport(Currencies.ETH.code(), 18);

        // Then
        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo(Currencies.ETH.name());
        assertThat(currency.code()).isEqualTo(Currencies.ETH.code());
        assertThat(currency.description()).isEqualTo(Currencies.ETH.description());
        assertThat(currency.logoUri()).isEqualTo(Currencies.ETH.logoUri());
        assertThat(currency.decimals()).isEqualTo(18);
        assertThat(currency.erc20()).isEmpty();

        verify(currencyStorage, times(1)).save(currency);
    }

    @Test
    void should_add_iso_currency_support() throws MalformedURLException {
        // Given
        when(isoCurrencyService.get(Currency.Code.of("EUR"))).thenReturn(Optional.of(Currency.fiat("Euro", Currency.Code.of("EUR"), 2)));
        when(imageStoragePort.storeImage(URI.create("https://euro.io"))).thenReturn(new URL("https://s3.euro.io"));

        // When
        final var currency = currencyService.addIsoCurrencySupport(Currency.Code.of("EUR"), "European currency", URI.create("https://euro.io"));

        // Then
        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Euro");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("EUR"));
        assertThat(currency.description()).contains("European currency");
        assertThat(currency.logoUri()).contains(URI.create("https://s3.euro.io"));
        assertThat(currency.decimals()).isEqualTo(2);
        assertThat(currency.erc20()).isEmpty();
        assertThat(currency.type()).isEqualTo(Currency.Type.FIAT);

        verify(currencyStorage, times(1)).save(currency);
    }


    @Test
    void should_prevent_adding_non_existing_iso_currency() {
        // Given
        when(isoCurrencyService.get(Currency.Code.of("ZZZ"))).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> currencyService.addIsoCurrencySupport(Currency.Code.of("ZZZ"), null, null))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Could not find ISO currency ZZZ");
    }


    @Test
    void should_return_existing_currency_upon_duplication() {
        // When
        final var currency = currencyService.addIsoCurrencySupport(Currencies.USD.code(), null, null);

        // Then
        assertThat(currency).isEqualTo(Currencies.USD);
    }

    @SneakyThrows
    @Test
    void should_allow_to_update_a_currency() {
        // Given
        final var initialCurrency = Currencies.USD;
        final var logoUrl = URI.create("https://usd.io");
        when(currencyStorage.get(initialCurrency.id())).thenReturn(Optional.of(initialCurrency));
        when(imageStoragePort.storeImage(logoUrl)).thenReturn(new URL("https://s3.usd.io"));

        // When
        final var currency = currencyService.updateCurrency(initialCurrency.id(), "United States Dollar", "US currency", logoUrl, 3);

        // Then
        assertThat(currency.id()).isEqualTo(initialCurrency.id());
        assertThat(currency.name()).isEqualTo("United States Dollar");
        assertThat(currency.code()).isEqualTo(initialCurrency.code());
        assertThat(currency.description()).contains("US currency");
        assertThat(currency.logoUri()).contains(URI.create("https://s3.usd.io"));
        assertThat(currency.decimals()).isEqualTo(3);
        assertThat(currency.erc20()).isEmpty();
        assertThat(currency.type()).isEqualTo(initialCurrency.type());

        verify(currencyStorage, times(1)).save(currency);
    }

    @Test
    void should_prevent_updating_non_existing_currency() {
        // Given
        final var currencyId = Currency.Id.random();
        when(currencyStorage.get(any())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> currencyService.updateCurrency(currencyId, "Some name", "Some descritpion", URI.create("https://usd.io"), 3))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));

        verify(currencyStorage, never()).save(any());
    }

    @Test
    void should_allow_partial_update_of_name() {
        // Given
        final var initialCurrency = Currencies.USD;
        when(currencyStorage.get(initialCurrency.id())).thenReturn(Optional.of(initialCurrency));

        // When
        final var currency = currencyService.updateCurrency(initialCurrency.id(), "United States Dollar", null, null, null);

        // Then
        assertThat(currency.id()).isEqualTo(initialCurrency.id());
        assertThat(currency.name()).isEqualTo("United States Dollar");
        assertThat(currency.code()).isEqualTo(initialCurrency.code());
        assertThat(currency.description()).isEqualTo(initialCurrency.description());
        assertThat(currency.logoUri()).isEqualTo(initialCurrency.logoUri());
        assertThat(currency.decimals()).isEqualTo(initialCurrency.decimals());
        assertThat(currency.erc20()).isEqualTo(initialCurrency.erc20());
        assertThat(currency.type()).isEqualTo(initialCurrency.type());

        verify(currencyStorage, times(1)).save(currency);
    }


    @Test
    void should_allow_partial_update_of_description() {
        // Given
        final var initialCurrency = Currencies.USD;
        when(currencyStorage.get(initialCurrency.id())).thenReturn(Optional.of(initialCurrency));

        // When
        final var currency = currencyService.updateCurrency(initialCurrency.id(), null, "US currency", null, null);

        // Then
        assertThat(currency.id()).isEqualTo(initialCurrency.id());
        assertThat(currency.name()).isEqualTo(initialCurrency.name());
        assertThat(currency.code()).isEqualTo(initialCurrency.code());
        assertThat(currency.description()).contains("US currency");
        assertThat(currency.logoUri()).isEqualTo(initialCurrency.logoUri());
        assertThat(currency.decimals()).isEqualTo(initialCurrency.decimals());
        assertThat(currency.erc20()).isEqualTo(initialCurrency.erc20());
        assertThat(currency.type()).isEqualTo(initialCurrency.type());

        verify(currencyStorage, times(1)).save(currency);
    }


    @SneakyThrows
    @Test
    void should_allow_partial_update_of_logo_url() {
        // Given
        final var initialCurrency = Currencies.USD;
        final var logoUrl = URI.create("https://usd.io");
        when(currencyStorage.get(initialCurrency.id())).thenReturn(Optional.of(initialCurrency));
        when(imageStoragePort.storeImage(logoUrl)).thenReturn(new URL("https://s3.usd.io"));

        // When
        final var currency = currencyService.updateCurrency(initialCurrency.id(), null, null, logoUrl, null);

        // Then
        assertThat(currency.id()).isEqualTo(initialCurrency.id());
        assertThat(currency.name()).isEqualTo(initialCurrency.name());
        assertThat(currency.code()).isEqualTo(initialCurrency.code());
        assertThat(currency.description()).isEqualTo(initialCurrency.description());
        assertThat(currency.logoUri()).contains(URI.create("https://s3.usd.io"));
        assertThat(currency.decimals()).isEqualTo(initialCurrency.decimals());
        assertThat(currency.erc20()).isEqualTo(initialCurrency.erc20());
        assertThat(currency.type()).isEqualTo(initialCurrency.type());

        verify(currencyStorage, times(1)).save(currency);
    }


    @Test
    void should_allow_partial_update_of_decimals() {
        // Given
        final var initialCurrency = Currencies.USD;
        when(currencyStorage.get(initialCurrency.id())).thenReturn(Optional.of(initialCurrency));

        // When
        final var currency = currencyService.updateCurrency(initialCurrency.id(), null, null, null, 3);

        // Then
        assertThat(currency.id()).isEqualTo(initialCurrency.id());
        assertThat(currency.name()).isEqualTo(initialCurrency.name());
        assertThat(currency.code()).isEqualTo(initialCurrency.code());
        assertThat(currency.description()).isEqualTo(initialCurrency.description());
        assertThat(currency.logoUri()).isEqualTo(initialCurrency.logoUri());
        assertThat(currency.decimals()).isEqualTo(3);
        assertThat(currency.erc20()).isEqualTo(initialCurrency.erc20());
        assertThat(currency.type()).isEqualTo(initialCurrency.type());

        verify(currencyStorage, times(1)).save(currency);
    }

    @SneakyThrows
    @Test
    void should_upload_currency_logo() {
        // Given
        final InputStream imageInputStream = mock(InputStream.class);
        final String imageUrl = faker.internet().image();

        // When
        when(imageStoragePort.storeImage(imageInputStream)).thenReturn(new URL(imageUrl));
        final URL url = currencyService.uploadLogo(imageInputStream);

        // Then
        assertThat(url.toString()).isEqualTo(imageUrl);
    }

    @SneakyThrows
    @Test
    void should_fail_to_get_quote_if_from_currency_not_found() {
        // Given
        when(currencyStorage.findByCode(Currencies.USD.code())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> currencyService.latestQuote(Currency.Code.USD, Currency.Code.EUR))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency USD not found");
    }

    @SneakyThrows
    @Test
    void should_fail_to_get_quote_if_to_currency_not_found() {
        // Given
        when(currencyStorage.findByCode(Currency.Code.USD)).thenReturn(Optional.of(Currencies.USD));
        when(currencyStorage.findByCode(Currency.Code.EUR)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> currencyService.latestQuote(Currency.Code.USD, Currency.Code.EUR))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency EUR not found");
    }

    @SneakyThrows
    @Test
    void should_fail_to_get_quote_if_quote_not_found() {
        // Given
        when(currencyStorage.findByCode(Currency.Code.USD)).thenReturn(Optional.of(Currencies.USD));
        when(currencyStorage.findByCode(Currency.Code.EUR)).thenReturn(Optional.of(Currencies.EUR));
        when(quoteStorage.nearest(eq(Currencies.USD.id()), eq(Currencies.EUR.id()), any())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> currencyService.latestQuote(Currency.Code.USD, Currency.Code.EUR))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Could not find quote from USD to EUR");
    }


    @SneakyThrows
    @Test
    void should_get_latest_quote() {
        // Given
        when(currencyStorage.findByCode(Currency.Code.USD)).thenReturn(Optional.of(Currencies.USD));
        when(currencyStorage.findByCode(Currency.Code.EUR)).thenReturn(Optional.of(Currencies.EUR));
        when(quoteStorage.nearest(eq(Currencies.USD.id()), eq(Currencies.EUR.id()), any()))
                .thenReturn(Optional.of(new Quote(Currencies.USD.id(), Currencies.EUR.id(), BigDecimal.valueOf(0.85), TIMESTAMP)));

        // When
        final var conversionRate = currencyService.latestQuote(Currency.Code.USD, Currency.Code.EUR);

        // Then
        assertThat(conversionRate).isEqualTo(BigDecimal.valueOf(0.85));
    }
}
