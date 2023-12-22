package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import onlydust.com.marketplace.api.domain.model.Currency;

public enum CurrencyEnumEntity {
    usd, eth, op, apt, stark, lords, usdc;

    public static CurrencyEnumEntity of(Currency currency) {
        return switch (currency) {
            case Eth -> CurrencyEnumEntity.eth;
            case Apt -> CurrencyEnumEntity.apt;
            case Op -> CurrencyEnumEntity.op;
            case Usd -> CurrencyEnumEntity.usd;
            case Stark -> CurrencyEnumEntity.stark;
            case Lords -> CurrencyEnumEntity.lords;
            case Usdc -> CurrencyEnumEntity.usdc;
        };
    }

    public Currency toDomain() {
        return switch (this) {
            case op -> Currency.Op;
            case apt -> Currency.Apt;
            case usd -> Currency.Usd;
            case eth -> Currency.Eth;
            case stark -> Currency.Stark;
            case lords -> Currency.Lords;
            case usdc -> Currency.Usdc;
        };
    }
}
