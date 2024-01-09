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

@Getter
public class Account {

    public static final Account.Id NOWHERE = Account.Id.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));

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
        mint(initialBalance);
    }

    public Id id() {
        return id;
    }

    public void mint(@NonNull PositiveAmount amount) {
        registerTransaction(new Transaction(NOWHERE, this.id, PositiveAmount.of(amount)));
    }

    public void burn(@NonNull PositiveAmount amount) {
        if (balance().isStrictlyLowerThan(amount)) {
            throw OnlyDustException.badRequest("Insufficient funds");
        }
        registerTransaction(new Transaction(this.id, NOWHERE, amount));
    }

    public void send(@NonNull Account recipient, @NonNull PositiveAmount amount) {
        if (balance().isStrictlyLowerThan(amount)) {
            throw OnlyDustException.badRequest("Insufficient funds");
        }
        final var transaction = new Transaction(this.id, recipient.id, amount);
        this.registerTransaction(transaction);
        recipient.registerTransaction(transaction);
    }

    public void refund(@NonNull Account recipient, @NonNull PositiveAmount amount) {
        if (balanceFrom(recipient.id).isStrictlyLowerThan(amount)) {
            throw OnlyDustException.badRequest("Cannot refund more than the amount received");
        }
        send(recipient, amount);
    }

    public Amount balance() {
        final var received = transactions.stream()
                .filter(tx -> tx.destination().equals(this.id))
                .map(Transaction::amount)
                .reduce(PositiveAmount.of(BigDecimal.ZERO, currency), PositiveAmount::plus);

        final var sent = transactions.stream()
                .filter(tx -> tx.origin().equals(this.id))
                .map(Transaction::amount)
                .reduce(PositiveAmount.of(BigDecimal.ZERO, currency), PositiveAmount::plus);

        return received.subtract(sent);
    }

    public Amount balanceFrom(@NonNull Account.Id from) {
        final var received = transactions.stream()
                .filter(tx -> tx.destination().equals(this.id) && tx.origin().equals(from))
                .map(Transaction::amount)
                .reduce(PositiveAmount.of(BigDecimal.ZERO, currency), PositiveAmount::plus);

        final var sent = transactions.stream()
                .filter(tx -> tx.destination().equals(from) && tx.origin().equals(this.id))
                .map(Transaction::amount)
                .reduce(PositiveAmount.of(BigDecimal.ZERO, currency), PositiveAmount::plus);

        return received.subtract(sent);
    }

    private void registerTransaction(@NonNull Transaction transaction) {
        if (!transaction.amount().getCurrency().equals(currency)) {
            throw OnlyDustException.badRequest("%s account cannot receive transactions in %s"
                    .formatted(currency, transaction.amount().getCurrency()));
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
