package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class CurrencyServiceTest {
    private final static ERC20 LORDS = new ERC20(Ethereum.contractAddress("0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0"), "Lords", "LORDS", 18, BigInteger.TEN);
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final ERC20Provider erc20Provider = mock(ERC20Provider.class);
    final CurrencyService currencyService = new CurrencyService(erc20Provider, currencyStorage);

    @Test
    void given_a_blockchain_evm_compatible_should_add_erc20_support() {
        // When
        when(erc20Provider.get(LORDS.address())).thenReturn(LORDS);
        currencyService.addERC20Support(Blockchain.ETHEREUM, LORDS.address());

        // Then
        ArgumentCaptor<Currency> currency = ArgumentCaptor.forClass(Currency.class);
        verify(currencyStorage, times(1)).save(currency.capture());

        final var capturedCurrency = currency.getValue();
        assertThat(capturedCurrency.id()).isNotNull();
        assertThat(capturedCurrency.name()).isEqualTo("Lords");
        assertThat(capturedCurrency.symbol()).isEqualTo("LORDS");
    }

    @Test
    void given_a_blockchain_not_evm_compatible_should_not_add_erc20_support() {
        // Given
        final var lordsAddress = Ethereum.contractAddress("0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0");

        // When
        assertThatThrownBy(() -> currencyService.addERC20Support(Blockchain.APTOS, lordsAddress))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Aptos is not EVM compatible");
    }
}
