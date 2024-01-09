package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Currency;

public interface Currencies {
    Currency OP = Currency.of(ERC20Tokens.OP);
    Currency USDC = Currency.of(ERC20Tokens.USDC);
    Currency LORDS = Currency.of(ERC20Tokens.LORDS);
    Currency STRK = new Currency("StarkNet Token", Currency.Code.of("STRK"));
    Currency USD = new Currency("US Dollar", Currency.Code.of("USD"));
    Currency ETH = new Currency("Ether", Currency.Code.of("ETH"));
}
