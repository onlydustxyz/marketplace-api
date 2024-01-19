package onlydust.com.marketplace.accounting.domain;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens.LORDS;
import static onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens.OP;
import static onlydust.com.marketplace.accounting.domain.stubs.Quotes.LORDS_USD;
import static onlydust.com.marketplace.accounting.domain.stubs.Quotes.USDC_USD;
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
    final ERC20Storage erc20Storage = mock(ERC20Storage.class);
    final ERC20ProviderFactory erc20ProviderFactory = new ERC20ProviderFactory(ethereumERC20Provider, optimismERC20Provider, starknetERC20Provider);
    final QuoteService quoteService = mock(QuoteService.class);
    final QuoteStorage quoteStorage = mock(QuoteStorage.class);
    final IsoCurrencyService isoCurrencyService = mock(IsoCurrencyService.class);
    CurrencyService currencyService;
    private final Faker faker = new Faker();
    private final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);

    @BeforeEach
    void setUp() {
        reset(currencyStorage, erc20Storage, ethereumERC20Provider, optimismERC20Provider, starknetERC20Provider, quoteService, quoteStorage,
                isoCurrencyService, imageStoragePort);
        when(erc20Storage.exists(any(), any())).thenReturn(false);
        when(currencyStorage.findByCode(any())).thenReturn(Optional.empty());
        when(currencyStorage.findByCode(Currency.Code.USD)).thenReturn(Optional.of(Currencies.USD));
        currencyService = new CurrencyService(erc20ProviderFactory, erc20Storage, currencyStorage, currencyMetadataService, quoteService, quoteStorage,
                isoCurrencyService, imageStoragePort);
    }

    @Test
    void should_add_erc20_support_on_ethereum() {
        //Given
        when(ethereumERC20Provider.get(LORDS.address())).thenReturn(Optional.of(LORDS));
        when(currencyMetadataService.get(LORDS)).thenReturn(Optional.of(new Currency.Metadata("LORDS", "Realms token", URI.create("https://realms.io"))));
        when(quoteService.currentPrice(any(), eq(Currencies.USD)))
                .then(i -> List.of(new Quote(((Currency) i.getArgument(0, List.class).get(0)).id(), Currencies.USD.id(), BigDecimal.valueOf(0.35))));

        // When
        final var currency = currencyService.addERC20Support(ETHEREUM, LORDS.address());

        // Then
        verify(currencyStorage, times(1)).save(currency);

        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Lords");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("LORDS"));
        assertThat(currency.description()).isEqualTo(Optional.of("Realms token"));
        assertThat(currency.logoUri()).isEqualTo(Optional.of(URI.create("https://realms.io")));

        verify(quoteStorage, times(1)).save(new Quote(currency.id(), Currencies.USD.id(), BigDecimal.valueOf(0.35)));
        verify(erc20Storage, times(1)).save(LORDS);
    }

    @Test
    void should_add_erc20_support_on_optimism() {
        // Given
        when(optimismERC20Provider.get(OP.address())).thenReturn(Optional.of(OP));

        // When
        final var currency = currencyService.addERC20Support(OPTIMISM, OP.address());

        // Then
        verify(currencyStorage, times(1)).save(currency);

        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Optimism");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("OP"));
        assertThat(currency.description()).isEqualTo(Optional.empty());
        assertThat(currency.logoUri()).isEqualTo(Optional.empty());
    }


    @Test
    void should_add_erc20_support_on_starknet() {
        //Given
        when(starknetERC20Provider.get(LORDS.address())).thenReturn(Optional.of(LORDS));
        when(currencyMetadataService.get(LORDS)).thenReturn(Optional.of(new Currency.Metadata("LORDS", "Realms token", URI.create("https://realms.io"))));
        when(quoteService.currentPrice(any(), eq(Currencies.USD)))
                .then(i -> List.of(new Quote(((Currency) i.getArgument(0, List.class).get(0)).id(), Currencies.USD.id(), BigDecimal.valueOf(0.35))));

        // When
        final var currency = currencyService.addERC20Support(STARKNET, LORDS.address());

        // Then
        verify(currencyStorage, times(1)).save(currency);

        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Lords");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("LORDS"));
        assertThat(currency.description()).isEqualTo(Optional.of("Realms token"));
        assertThat(currency.logoUri()).isEqualTo(Optional.of(URI.create("https://realms.io")));

        verify(quoteStorage, times(1)).save(new Quote(currency.id(), Currencies.USD.id(), BigDecimal.valueOf(0.35)));
        verify(erc20Storage, times(1)).save(LORDS);
    }

    @Test
    void should_not_add_erc20_support_on_unsupported_blockchain() {
        // When
        assertThatThrownBy(() -> currencyService.addERC20Support(APTOS, LORDS.address()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("ERC20 tokens on Aptos are not supported");
    }

    @Test
    void given_a_wrong_address_should_not_add_erc20_support() {
        // Given
        final var invalidAddress = Ethereum.contractAddress("0x388C818CA8B9251b393131C08a736A67ccB19297");
        when(ethereumERC20Provider.get(LORDS.address())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> currencyService.addERC20Support(ETHEREUM, invalidAddress))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Could not find a valid ERC20 contract at address 0x388C818CA8B9251b393131C08a736A67ccB19297 on Ethereum");
    }

    @Test
    void should_not_store_quote_if_not_found() {
        // Given
        when(ethereumERC20Provider.get(LORDS.address())).thenReturn(Optional.of(LORDS));
        when(quoteService.currentPrice(List.of(Currencies.LORDS), Currencies.USD)).thenReturn(List.of());

        // When
        final var currency = currencyService.addERC20Support(ETHEREUM, LORDS.address());

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
    void should_not_add_duplicate_currencies() {
        // Given
        when(ethereumERC20Provider.get(LORDS.address())).thenReturn(Optional.of(LORDS));
        when(currencyStorage.findByCode(Currency.Code.of("LORDS"))).thenReturn(Optional.of(Currencies.LORDS));

        // When
        final var currency = currencyService.addERC20Support(ETHEREUM, LORDS.address());

        // Then
        assertThat(currency).isEqualTo(Currencies.LORDS);
        verify(currencyStorage, never()).save(any());
        verify(quoteService, never()).currentPrice(any(), any());
        verify(quoteStorage, never()).save(any());
    }

    @Test
    void should_reject_duplicate_erc20() {
        // Given
        when(ethereumERC20Provider.get(LORDS.address())).thenReturn(Optional.of(LORDS));
        when(currencyStorage.findByCode(Currency.Code.of("LORDS"))).thenReturn(Optional.of(Currencies.LORDS));
        when(erc20Storage.exists(ETHEREUM, LORDS.address())).thenReturn(true);

        // When
        assertThatThrownBy(() -> currencyService.addERC20Support(ETHEREUM, LORDS.address()))
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
        final var currencies = List.of(Currencies.USDC, Currencies.LORDS, Currencies.STRK);

        when(currencyStorage.all()).thenReturn(currencies);
        when(quoteService.currentPrice(currencies, Currencies.USD))
                .thenReturn(List.of(USDC_USD, LORDS_USD));

        // When
        currencyService.refreshQuotes();

        // Then
        verify(quoteStorage, times(1)).save(USDC_USD);
        verify(quoteStorage, times(1)).save(LORDS_USD);
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
    void should_add_iso_currency_support() {
        // Given
        when(isoCurrencyService.get(Currency.Code.of("EUR"))).thenReturn(Optional.of(Currency.fiat("Euro", Currency.Code.of("EUR"), 2)));

        // When
        final var currency = currencyService.addIsoCurrencySupport(Currency.Code.of("EUR"));

        // Then
        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Euro");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("EUR"));
        assertThat(currency.description()).isEmpty();
        assertThat(currency.logoUri()).isEmpty();
        assertThat(currency.decimals()).isEqualTo(2);
        assertThat(currency.erc20()).isEmpty();
        assertThat(currency.type()).isEqualTo(Currency.Type.FIAT);
        assertThat(currency.standard()).contains(Currency.Standard.ISO4217);

        verify(currencyStorage, times(1)).save(currency);
    }


    @Test
    void should_return_existing_currency_upon_duplication() {
        // Given
        when(isoCurrencyService.get(Currency.Code.of("ZZZ"))).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> currencyService.addIsoCurrencySupport(Currency.Code.of("ZZZ")))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Could not find ISO currency ZZZ");
    }


    @Test
    void should_prevent_adding_non_existing_iso_currency() {
        // When
        final var currency = currencyService.addIsoCurrencySupport(Currency.Code.USD);

        // Then
        assertThat(currency).isEqualTo(Currencies.USD);
        verify(currencyStorage, never()).save(any());
        verify(isoCurrencyService, never()).get(any());
    }

    @Test
    void should_allow_to_update_a_currency() {
        // Given
        final var initialCurrency = Currencies.USD;
        when(currencyStorage.get(initialCurrency.id())).thenReturn(Optional.of(initialCurrency));

        // When
        final var currency = currencyService.updateCurrency(initialCurrency.id(), "United States Dollar", "US currency", URI.create("https://usd.io"), 3);

        // Then
        assertThat(currency.id()).isEqualTo(initialCurrency.id());
        assertThat(currency.name()).isEqualTo("United States Dollar");
        assertThat(currency.code()).isEqualTo(initialCurrency.code());
        assertThat(currency.description()).contains("US currency");
        assertThat(currency.logoUri()).contains(URI.create("https://usd.io"));
        assertThat(currency.decimals()).isEqualTo(3);
        assertThat(currency.erc20()).isEmpty();
        assertThat(currency.type()).isEqualTo(initialCurrency.type());
        assertThat(currency.standard()).isEqualTo(initialCurrency.standard());

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
        assertThat(currency.standard()).isEqualTo(initialCurrency.standard());

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
        assertThat(currency.standard()).isEqualTo(initialCurrency.standard());

        verify(currencyStorage, times(1)).save(currency);
    }


    @Test
    void should_allow_partial_update_of_logo_url() {
        // Given
        final var initialCurrency = Currencies.USD;
        when(currencyStorage.get(initialCurrency.id())).thenReturn(Optional.of(initialCurrency));

        // When
        final var currency = currencyService.updateCurrency(initialCurrency.id(), null, null, URI.create("https://usd.io"), null);

        // Then
        assertThat(currency.id()).isEqualTo(initialCurrency.id());
        assertThat(currency.name()).isEqualTo(initialCurrency.name());
        assertThat(currency.code()).isEqualTo(initialCurrency.code());
        assertThat(currency.description()).isEqualTo(initialCurrency.description());
        assertThat(currency.logoUri()).contains(URI.create("https://usd.io"));
        assertThat(currency.decimals()).isEqualTo(initialCurrency.decimals());
        assertThat(currency.erc20()).isEqualTo(initialCurrency.erc20());
        assertThat(currency.type()).isEqualTo(initialCurrency.type());
        assertThat(currency.standard()).isEqualTo(initialCurrency.standard());

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
        assertThat(currency.standard()).isEqualTo(initialCurrency.standard());

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
}
