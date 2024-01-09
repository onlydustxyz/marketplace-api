package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;

public record Currency(@NonNull String code) {
    public static Currency Usd = new Currency("USD");
    public static Currency Eth = new Currency("ETH");

    @Override
    public String toString() {
        return code;
    }
}
