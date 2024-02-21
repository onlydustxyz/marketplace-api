package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import onlydust.com.marketplace.project.domain.model.Currency;

public enum CurrencyEnumEntity {
    usd, eth, op, apt, strk, lords, usdc;

    public static CurrencyEnumEntity of(Currency currency) {
        return switch (currency) {
            case ETH -> CurrencyEnumEntity.eth;
            case APT -> CurrencyEnumEntity.apt;
            case OP -> CurrencyEnumEntity.op;
            case USD -> CurrencyEnumEntity.usd;
            case STRK -> CurrencyEnumEntity.strk;
            case LORDS -> CurrencyEnumEntity.lords;
            case USDC -> CurrencyEnumEntity.usdc;
        };
    }

    public static CurrencyEnumEntity of(onlydust.com.marketplace.accounting.domain.model.Currency currency) {
        return CurrencyEnumEntity.valueOf(currency.code().toString().toLowerCase());
    }

    public Currency toDomain() {
        return switch (this) {
            case op -> Currency.OP;
            case apt -> Currency.APT;
            case usd -> Currency.USD;
            case eth -> Currency.ETH;
            case strk -> Currency.STRK;
            case lords -> Currency.LORDS;
            case usdc -> Currency.USDC;
        };
    }
}
