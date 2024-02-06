package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyTest {

    @Test
    void should_create_fiat_currency() {
        final var currency = Currency.fiat("US Dollar", Currency.Code.of("USD"), 2);
        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("US Dollar");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("USD"));
        assertThat(currency.type()).isEqualTo(Currency.Type.FIAT);
        assertThat(currency.standard()).contains(Currency.Standard.ISO4217);
        assertThat(currency.decimals()).isEqualTo(2);
        assertThat(currency.description()).isEmpty();
        assertThat(currency.logoUri()).isEmpty();
        assertThat(currency.erc20()).isEmpty();
    }

    @Test
    void should_create_crypto_native_currency() {
        final var currency = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);
        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("Ether");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("ETH"));
        assertThat(currency.type()).isEqualTo(Currency.Type.CRYPTO);
        assertThat(currency.standard()).isEmpty();
        assertThat(currency.decimals()).isEqualTo(18);
        assertThat(currency.description()).isEmpty();
        assertThat(currency.logoUri()).isEmpty();
        assertThat(currency.erc20()).isEmpty();
    }

    @Test
    void should_create_currency_from_erc20() {
        final var currency = Currency.of(ERC20Tokens.ETH_USDC);
        assertThat(currency.id()).isNotNull();
        assertThat(currency.name()).isEqualTo("USD Coin");
        assertThat(currency.code()).isEqualTo(Currency.Code.of("USDC"));
        assertThat(currency.type()).isEqualTo(Currency.Type.CRYPTO);
        assertThat(currency.standard()).contains(Currency.Standard.ERC20);
        assertThat(currency.decimals()).isEqualTo(6);
        assertThat(currency.description()).isEmpty();
        assertThat(currency.logoUri()).isEmpty();
        assertThat(currency.erc20()).contains(ERC20Tokens.ETH_USDC);
    }

    @ParameterizedTest
    @EnumSource(value = Network.class, names = {"SEPA", "SWIFT"})
    void should_return_payable_currency_for_fiat(Network network) {
        // Given
        final var euro = Currency.fiat("Euro", Currency.Code.of("EUR"), 2);

        // When
        final var payableCurrency = euro.forNetwork(network);

        // Then
        assertThat(payableCurrency.id()).isEqualTo(euro.id());
        assertThat(payableCurrency.code()).isEqualTo(euro.code());
        assertThat(payableCurrency.name()).isEqualTo(euro.name());
        assertThat(payableCurrency.type()).isEqualTo(Currency.Type.FIAT);
        assertThat(payableCurrency.standard()).contains(Currency.Standard.ISO4217);
        assertThat(payableCurrency.blockchain()).isNotPresent();
        assertThat(payableCurrency.address()).isNotPresent();
    }

    @ParameterizedTest
    @EnumSource(value = Network.class, names = {"SEPA", "SWIFT"}, mode = EnumSource.Mode.EXCLUDE)
    void should_throw_when_network_is_not_supported_for_fiat(Network network) {
        // Given
        final var euro = Currency.fiat("Euro", Currency.Code.of("EUR"), 2);

        // When
        assertThatThrownBy(() -> euro.forNetwork(network))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessageContaining("Currency EUR is not supported on network");
    }

    @Test
    void should_return_payable_currency_for_native_eth() {
        // Given
        final var ether = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);

        // When
        final var payableCurrency = ether.forNetwork(Network.ETHEREUM);

        // Then
        assertThat(payableCurrency.id()).isEqualTo(ether.id());
        assertThat(payableCurrency.code()).isEqualTo(ether.code());
        assertThat(payableCurrency.name()).isEqualTo(ether.name());
        assertThat(payableCurrency.type()).isEqualTo(Currency.Type.CRYPTO);
        assertThat(payableCurrency.standard()).isNotPresent();
        assertThat(payableCurrency.blockchain()).contains(Blockchain.ETHEREUM);
        assertThat(payableCurrency.address()).isNotPresent();
    }

    @Test
    void should_return_payable_currency_for_native_op() {
        // Given
        final var op = Currency.crypto("Optimism", Currency.Code.of("OP"), 9);

        // When
        final var payableCurrency = op.forNetwork(Network.OPTIMISM);

        // Then
        assertThat(payableCurrency.id()).isEqualTo(op.id());
        assertThat(payableCurrency.code()).isEqualTo(op.code());
        assertThat(payableCurrency.name()).isEqualTo(op.name());
        assertThat(payableCurrency.type()).isEqualTo(Currency.Type.CRYPTO);
        assertThat(payableCurrency.standard()).isNotPresent();
        assertThat(payableCurrency.blockchain()).contains(Blockchain.OPTIMISM);
        assertThat(payableCurrency.address()).isNotPresent();
    }

    @Test
    void should_return_payable_currency_for_erc20_strk() {
        // Given
        final var strk = Currency.of(ERC20Tokens.STRK);

        // When
        final var payableCurrency = strk.forNetwork(Network.ETHEREUM);

        // Then
        assertThat(payableCurrency.id()).isEqualTo(strk.id());
        assertThat(payableCurrency.code()).isEqualTo(strk.code());
        assertThat(payableCurrency.name()).isEqualTo(strk.name());
        assertThat(payableCurrency.type()).isEqualTo(Currency.Type.CRYPTO);
        assertThat(payableCurrency.standard()).contains(Currency.Standard.ERC20);
        assertThat(payableCurrency.blockchain()).contains(Blockchain.ETHEREUM);
        assertThat(payableCurrency.address()).contains(ERC20Tokens.STRK.getAddress());
    }

    @Test
    void should_throw_if_erc20_is_not_supported() {
        // Given
        final var usdc = Currency.of(ERC20Tokens.ETH_USDC);

        // When
        assertThatThrownBy(() -> usdc.forNetwork(Network.OPTIMISM))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency USDC is not supported on network OPTIMISM");
    }
}