package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ledger {
    final @NonNull Id id;
    final @NonNull List<Transaction> transactions = new ArrayList<>();

    public Ledger() {
        this.id = Id.random();
    }

    public Id id() {
        return this.id;
    }

    public PositiveAmount balance() {
        return PositiveAmount.of(transactions.stream()
                .map(Transaction::amount)
                .reduce(Amount.ZERO, Amount::add));
    }

    public void credit(PositiveAmount amount) {
        transactions.add(new Transaction(amount));
    }

    public void debit(PositiveAmount amount) {
        if (balance().isStrictlyLowerThan(amount))
            throw OnlyDustException.badRequest("Not enough fund on ledger %s".formatted(id));

        transactions.add(new Transaction(amount.negate()));
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

    private record Transaction(Amount amount) {
    }
}
