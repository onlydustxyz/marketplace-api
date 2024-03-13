package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.net.URI;

public interface Currencies {
    Currency OP = Currency.of(ERC20Tokens.OP);
    Currency USDC = Currency.of(ERC20Tokens.ETH_USDC).withERC20(ERC20Tokens.OP_USDC);
    Currency LORDS = Currency.of(ERC20Tokens.LORDS);
    Currency STRK = Currency.of(ERC20Tokens.STRK).withERC20(ERC20Tokens.STARKNET_STRK);
    Currency USD = Currency.fiat("US Dollar", Currency.Code.of("USD"), 2);
    Currency EUR = Currency.fiat("Euro", Currency.Code.of("EUR"), 2);
    Currency ETH = Currency.crypto("Ether", Currency.Code.of("ETH"), 18)
            .withMetadata(new Currency.Metadata("Ether", "Ethereum (ETH) is a cryptocurrency",
                    URI.create("https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png")));
}
