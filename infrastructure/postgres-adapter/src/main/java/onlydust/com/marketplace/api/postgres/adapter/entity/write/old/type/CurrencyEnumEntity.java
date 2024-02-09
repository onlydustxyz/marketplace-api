package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import onlydust.com.marketplace.api.domain.model.Currency;

public enum CurrencyEnumEntity {
    usd, eth, op, apt, strk, lords, usdc;

    public static CurrencyEnumEntity of(Currency currency) {
        return switch (currency) {
            case Eth -> CurrencyEnumEntity.eth;
            case Apt -> CurrencyEnumEntity.apt;
            case Op -> CurrencyEnumEntity.op;
            case Usd -> CurrencyEnumEntity.usd;
            case Strk -> CurrencyEnumEntity.strk;
            case Lords -> CurrencyEnumEntity.lords;
            case Usdc -> CurrencyEnumEntity.usdc;
        };
    }

    public static CurrencyEnumEntity of(onlydust.com.marketplace.accounting.domain.model.Currency currency) {
        return CurrencyEnumEntity.valueOf(currency.code().toString().toLowerCase());
    }

    public Currency toDomain() {
        return switch (this) {
            case op -> Currency.Op;
            case apt -> Currency.Apt;
            case usd -> Currency.Usd;
            case eth -> Currency.Eth;
            case strk -> Currency.Strk;
            case lords -> Currency.Lords;
            case usdc -> Currency.Usdc;
        };
    }
}
