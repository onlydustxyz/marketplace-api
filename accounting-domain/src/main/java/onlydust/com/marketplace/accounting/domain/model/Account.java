package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
public class Account {

    @NonNull
    private final Account.Id id;
    @NonNull
    private final Currency currency;
    @NonNull
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(@NonNull Currency currency) {
        this.id = Id.random();
        this.currency = currency;
    }

    public Account(@NonNull PositiveAmount initialBalance) {
        this.id = Id.random();
        this.currency = initialBalance.getCurrency();
        registerTransaction(new Transaction(null, this.id, initialBalance));
    }

    public Id id() {
        return id;
    }

    public void registerExternalTransfer(@NonNull Amount amount) {
        if (amount.isPositive()) {
            registerTransaction(new Transaction(null, this.id, PositiveAmount.of(amount)));
        } else {
            registerTransaction(new Transaction(this.id, null, PositiveAmount.of(amount.negate())));
        }
    }

    public void sendAmountTo(@NonNull Account recipient, @NonNull PositiveAmount amount) {
        final var transaction = new Transaction(this.id, recipient.id, amount);
        this.registerTransaction(transaction);
        recipient.registerTransaction(transaction);
    }

    public void sendRefundTo(@NonNull Account recipient, @NonNull PositiveAmount amount) {
        final var transaction = new Transaction(this.id, recipient.id, amount);

        final var subBalance = filteredBalanceWithMoreTransactions(recipient.id, transaction);
        if (subBalance.isNegative()) {
            throw OnlyDustException.badRequest("Cannot refund more than the amount received");
        }

        this.registerTransaction(transaction);
        recipient.registerTransaction(transaction);
    }

    public Amount balance() {
        return balanceWithMoreTransactions();
    }

    private Amount balanceWithMoreTransactions(Transaction... someOtherTransactions) {
        return filteredBalanceWithMoreTransactions(null, someOtherTransactions);
    }

    private Amount filteredBalanceWithMoreTransactions(Id otherAccountId, Transaction... someOtherTransactions) {
        return Stream.concat(transactions.stream(), Stream.of(someOtherTransactions))
                .filter(tx -> otherAccountId == null || otherAccountId.equals(tx.origin()) || otherAccountId.equals(tx.destination()))
                .map(tx -> this.id.equals(tx.origin()) ? tx.amount().negate() : tx.amount())
                .reduce(Amount.of(BigDecimal.ZERO, currency), Amount::plus);
    }

    private void registerTransaction(@NonNull Transaction transaction) {
        final var newBalance = balanceWithMoreTransactions(transaction);
        if (newBalance.isNegative()) {
            throw OnlyDustException.badRequest("Insufficient funds");
        }
        transactions.add(transaction);
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    
    }
}
