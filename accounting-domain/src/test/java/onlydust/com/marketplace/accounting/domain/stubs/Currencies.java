package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.net.URI;

public interface Currencies {
    Currency OP = Currency.of(ERC20Tokens.OP);
    Currency USDC = Currency.of(ERC20Tokens.ETH_USDC);
    Currency LORDS = Currency.of(ERC20Tokens.LORDS);
    Currency STRK = Currency.of(ERC20Tokens.STRK);
    Currency USD = Currency.fiat("US Dollar", Currency.Code.USD, 2);
    Currency EUR = Currency.fiat("Euro", Currency.Code.EUR, 2);
    Currency APT = Currency.crypto("Aptos", Currency.Code.APT, 9);
    Currency ETH = Currency.crypto("Ether", Currency.Code.ETH, 18)
            .withMetadata(new Currency.Metadata("Ether", "Ethereum (ETH) is a cryptocurrency",
                    URI.create("https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png")));
}
