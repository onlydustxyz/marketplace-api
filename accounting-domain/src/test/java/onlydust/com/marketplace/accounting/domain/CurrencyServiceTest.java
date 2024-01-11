package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;
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
    final ERC20ProviderFactory erc20ProviderFactory = new ERC20ProviderFactory(ethereumERC20Provider, optimismERC20Provider);
    final QuoteService quoteService = mock(QuoteService.class);
    final QuoteStorage quoteStorage = mock(QuoteStorage.class);
    final CurrencyService currencyService = new CurrencyService(erc20ProviderFactory, currencyStorage, currencyMetadataService, quoteService, quoteStorage);

    @BeforeEach
    void setUp() {
        reset(currencyStorage, ethereumERC20Provider, optimismERC20Provider, quoteService, quoteStorage);
        when(currencyStorage.exists(any())).thenReturn(false);
    }

    @Test
    void should_add_erc20_support_on_ethereum() {
        //Given
        when(ethereumERC20Provider.get(LORDS.address())).thenReturn(Optional.of(LORDS));
        when(quoteService.currentPrice(any(), LORDS, Currency.Code.USD)).thenReturn(Optional.of(LORDS_USD));
        when(currencyMetadataService.get(LORDS)).thenReturn(Optional.of(new Currency.Metadata("Realms token", URI.create("https" +
                                                                                                                         "://realms.io"))));

        // When
        currencyService.addERC20Support(ETHEREUM, LORDS.address());

        // Then
        ArgumentCaptor<Currency> currency = ArgumentCaptor.forClass(Currency.class);
        verify(currencyStorage, times(1)).save(currency.capture());

        final var capturedCurrency = currency.getValue();
        assertThat(capturedCurrency.id()).isNotNull();
        assertThat(capturedCurrency.name()).isEqualTo("Lords");
        assertThat(capturedCurrency.code()).isEqualTo(Currency.Code.of("LORDS"));
        assertThat(capturedCurrency.description()).isEqualTo(Optional.of("Realms token"));
        assertThat(capturedCurrency.logoUri()).isEqualTo(Optional.of(URI.create("https://realms.io")));

        verify(quoteStorage, times(1)).save(LORDS_USD);
    }

    @Test
    void should_add_erc20_support_on_optimism() {
        // Given
        when(optimismERC20Provider.get(OP.address())).thenReturn(Optional.of(OP));

        // When
        currencyService.addERC20Support(OPTIMISM, OP.address());

        // Then
        ArgumentCaptor<Currency> currency = ArgumentCaptor.forClass(Currency.class);
        verify(currencyStorage, times(1)).save(currency.capture());

        final var capturedCurrency = currency.getValue();
        assertThat(capturedCurrency.id()).isNotNull();
        assertThat(capturedCurrency.name()).isEqualTo("Optimism");
        assertThat(capturedCurrency.code()).isEqualTo(Currency.Code.of("OP"));
        assertThat(capturedCurrency.description()).isEqualTo(Optional.empty());
        assertThat(capturedCurrency.logoUri()).isEqualTo(Optional.empty());
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
        when(quoteService.currentPrice(any(), LORDS, Currency.Code.USD)).thenReturn(Optional.empty());

        // When
        currencyService.addERC20Support(ETHEREUM, LORDS.address());

        // Then
        verify(currencyStorage, times(1)).save(any());
        verify(quoteStorage, never()).save(any());
    }

    @Test
    void should_not_add_duplicate_currencies() {
        // Given
        when(ethereumERC20Provider.get(LORDS.address())).thenReturn(Optional.of(LORDS));
        when(currencyStorage.exists(Currency.Code.of("LORDS"))).thenReturn(true);

        // When
        currencyService.addERC20Support(ETHEREUM, LORDS.address());

        // Then
        verify(currencyStorage, never()).save(any());
        verify(quoteService, never()).currentPrice(any(), any(ERC20.class), any());
        verify(quoteStorage, never()).save(any());
    }

    @Test
    void should_refresh_quotes() {
        // Given
        final var currencies = List.of(Currencies.USDC, Currencies.LORDS, Currencies.STRK);

        when(currencyStorage.all()).thenReturn(currencies);
        when(quoteService.currentPrice(currencies.stream().map(Currency::id).toList(), Currency.Code.USD))
                .thenReturn(List.of(Optional.of(USDC_USD), Optional.of(LORDS_USD), Optional.empty()));

        // When
        currencyService.refreshQuotes();

        // Then
        verify(quoteStorage, times(1)).save(USDC_USD, LORDS_USD);
    }
}
