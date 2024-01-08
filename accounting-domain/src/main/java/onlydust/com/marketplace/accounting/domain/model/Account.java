package onlydust.com.marketplace.accounting.domain.model;

import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;

@Getter
public class Account {
    
    @NonNull
    private Amount balance;

    public Account(@NonNull Currency currency) {
        this.balance = Amount.of(BigDecimal.ZERO, currency);
    }

    public Account(@NonNull Amount initialBalance) {
        this.balance = initialBalance;
    }

    public void registerTransfer(@NonNull Amount amount) {
        final var newBalance = balance.plus(amount);
        if (newBalance.isNegative()) {
            throw OnlyDustException.badRequest("Insufficient funds");
        }
        balance = newBalance;
    }

    public void transferTo(@NonNull Account recipient, @NonNull Amount amount) {
        this.registerTransfer(amount.negate());
        recipient.registerTransfer(amount);
    }
}
