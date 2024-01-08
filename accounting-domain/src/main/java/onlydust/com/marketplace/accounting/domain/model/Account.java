package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;

public class Account {
    private Amount balance;

    public Account(Currency currency) {
        this.balance = Amount.of(BigDecimal.ZERO, currency);
    }

    public Account(Amount initialBalance) {
        this.balance = initialBalance;
    }

    public void transfer(Amount amount) {
        final var newBalance = balance.plus(amount);
        if (newBalance.isNegative()) {
            throw OnlyDustException.badRequest("Insufficient funds");
        }
        balance = newBalance;
    }
}
