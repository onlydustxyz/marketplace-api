package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}