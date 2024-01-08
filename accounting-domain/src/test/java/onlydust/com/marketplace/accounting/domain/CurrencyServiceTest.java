package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteService;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class CurrencyServiceTest {
    private final static ERC20 LORDS = new ERC20(Ethereum.contractAddress("0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0"), "Lords", "LORDS", 18, BigInteger.TEN);
    private final static ERC20 OP = new ERC20(Optimism.contractAddress("0x4200000000000000000000000000000000000042"), "Optimism", "OP", 18, BigInteger.TEN);
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final ERC20Provider ethereumERC20Provider = mock(ERC20Provider.class);
    final ERC20Provider optimismERC20Provider = mock(ERC20Provider.class);
    final ERC20ProviderFactory erc20ProviderFactory = new ERC20ProviderFactory(ethereumERC20Provider, optimismERC20Provider);
    final QuoteService quoteService = mock(QuoteService.class);
    final QuoteStorage quoteStorage = mock(QuoteStorage.class);
    final CurrencyService currencyService = new CurrencyService(erc20ProviderFactory, currencyStorage, quoteService, quoteStorage);

    @Test
    void should_add_erc20_support_on_ethereum() {
        // When
        when(ethereumERC20Provider.get(LORDS.address())).thenReturn(Optional.of(LORDS));
        when(quoteService.currentPrice(LORDS, Currency.Code.USD)).thenReturn(BigDecimal.valueOf(0.35));
        currencyService.addERC20Support(ETHEREUM, LORDS.address());

        // Then
        ArgumentCaptor<Currency> currency = ArgumentCaptor.forClass(Currency.class);
        verify(currencyStorage, times(1)).save(currency.capture());

        final var capturedCurrency = currency.getValue();
        assertThat(capturedCurrency.id()).isNotNull();
        assertThat(capturedCurrency.name()).isEqualTo("Lords");
        assertThat(capturedCurrency.code()).isEqualTo(Currency.Code.of("LORDS"));

        verify(quoteStorage, times(1))
                .save(new Quote(capturedCurrency.id(), Currency.Code.USD, BigDecimal.valueOf(0.35)));
    }

    @Test
    void should_add_erc20_support_on_optimism() {
        // When
        when(optimismERC20Provider.get(OP.address())).thenReturn(Optional.of(OP));
        currencyService.addERC20Support(OPTIMISM, OP.address());

        // Then
        ArgumentCaptor<Currency> currency = ArgumentCaptor.forClass(Currency.class);
        verify(currencyStorage, times(1)).save(currency.capture());

        final var capturedCurrency = currency.getValue();
        assertThat(capturedCurrency.id()).isNotNull();
        assertThat(capturedCurrency.name()).isEqualTo("Optimism");
        assertThat(capturedCurrency.code()).isEqualTo(Currency.Code.of("OP"));
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

        // When
        when(ethereumERC20Provider.get(LORDS.address())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> currencyService.addERC20Support(ETHEREUM, invalidAddress))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Could not find a valid ERC20 contract at address 0x388C818CA8B9251b393131C08a736A67ccB19297 on Ethereum");
    }
}
