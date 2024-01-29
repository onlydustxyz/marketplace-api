package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ledger {
    @EqualsAndHashCode.Include
    final @NonNull Id id;
    final @NonNull Object ownerId;
    final @NonNull Currency currency;
    final @NonNull List<Transaction> transactions = new ArrayList<>();

    public <OwnerId> Ledger(final @NonNull OwnerId ownerId, final @NonNull Currency currency) {
        this.id = Id.random();
        this.ownerId = ownerId;
        this.currency = currency;
    }

    public static Ledger of(Ledger other) {
        final var ledger = new Ledger(other.id, other.ownerId, other.currency);
        ledger.transactions.addAll(other.transactions);
        return ledger;
    }

    public Id id() {
        return this.id;
    }

    public Object ownerId() {
        return this.ownerId;
    }

    public Currency currency() {
        return this.currency;
    }

    public PositiveAmount unlockedBalance(final @NonNull Network network) {
        return PositiveAmount.of(transactions.stream()
                .filter(transaction -> transaction.network.equals(network))
                .takeWhile(transaction -> transaction.lockedUntil == null || transaction.lockedUntil.isBefore(ZonedDateTime.now()))
                .map(Transaction::amount)
                .reduce(Amount.ZERO, Amount::add));
    }

    public void credit(PositiveAmount amount, Network network, ZonedDateTime lockedUntil) {
        transactions.add(new Transaction(amount, network, lockedUntil));
    }

    public void debit(PositiveAmount amount, Network network) {
        if (unlockedBalance(network).isStrictlyLowerThan(amount))
            throw badRequest("Not enough fund on ledger %s on network %s".formatted(id, network));

        transactions.add(new Transaction(amount.negate(), network, null));
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

    private record Transaction(@NonNull Amount amount, @NonNull Network network, ZonedDateTime lockedUntil) {
    }
}
