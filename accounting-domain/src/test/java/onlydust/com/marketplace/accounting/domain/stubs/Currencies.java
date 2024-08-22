package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.net.URI;

public interface Currencies {
    Currency OP = Currency.of(ERC20Tokens.OP)
            .withMetadata(new Currency.Metadata(11840, null, null, null));
    Currency USDC = Currency.of(ERC20Tokens.ETH_USDC)
            .withMetadata(new Currency.Metadata(3408, null, null, null));
    Currency LORDS = Currency.of(ERC20Tokens.LORDS)
            .withMetadata(new Currency.Metadata(17445, null, null, null));
    Currency STRK = Currency.of(ERC20Tokens.STRK)
            .withMetadata(new Currency.Metadata(22691, null, null, null));
    Currency USD = Currency.fiat("US Dollar", Currency.Code.USD, 2)
            .withMetadata(new Currency.Metadata(2781, null, null, null));
    Currency EUR = Currency.fiat("Euro", Currency.Code.EUR, 2)
            .withMetadata(new Currency.Metadata(2790, null, null, null));
    Currency APT = Currency.crypto("Aptos", Currency.Code.APT, 9)
            .withMetadata(new Currency.Metadata(21794, null, null, null));
    Currency ETH = Currency.crypto("Ether", Currency.Code.ETH, 18)
            .withMetadata(new Currency.Metadata(1027, "Ether", "Ethereum (ETH) is a cryptocurrency",
                    URI.create("https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png")));
}
