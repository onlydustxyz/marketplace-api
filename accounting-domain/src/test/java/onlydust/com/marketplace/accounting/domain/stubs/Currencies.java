package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Currency;

public interface Currencies {
    Currency OP = Currency.of(ERC20Tokens.OP);
    Currency USDC = Currency.of(ERC20Tokens.USDC);
    Currency LORDS = Currency.of(ERC20Tokens.LORDS);
    Currency STRK = Currency.of(ERC20Tokens.STRK);
    Currency USD = Currency.fiat("US Dollar", Currency.Code.of("USD"), 2);
    Currency ETH = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);
}
