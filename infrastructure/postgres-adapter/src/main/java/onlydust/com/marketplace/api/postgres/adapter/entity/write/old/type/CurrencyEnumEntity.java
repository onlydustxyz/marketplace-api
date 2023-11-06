package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import onlydust.com.marketplace.api.domain.model.Currency;

public enum CurrencyEnumEntity {
    usd, eth, op, apt, stark;

    public Currency toDomain() {
        return switch (this) {
            case op -> Currency.Op;
            case apt -> Currency.Apt;
            case usd -> Currency.Usd;
            case eth -> Currency.Eth;
            case stark -> Currency.Stark;
        };
    }
}
