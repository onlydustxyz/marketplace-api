package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.accounting.domain.model.PositiveAmount.min;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ledger {
    @EqualsAndHashCode.Include
    final @NonNull Id id;
    final @NonNull Object ownerId;
    final @NonNull Currency currency;
    ZonedDateTime lockedUntil;

    @Getter
    final @NonNull List<Transaction> transactions = new ArrayList<>();

    public <OwnerId> Ledger(final @NonNull OwnerId ownerId, final @NonNull Currency currency, ZonedDateTime lockedUntil) {
        this.id = Id.random();
        this.ownerId = ownerId;
        this.currency = currency;
        this.lockedUntil = lockedUntil;
    }

    public <OwnerId> Ledger(final @NonNull OwnerId ownerId, final @NonNull Currency currency) {
        this.id = Id.random();
        this.ownerId = ownerId;
        this.currency = currency;
    }

    public static Ledger of(Ledger other) {
        final var ledger = new Ledger(other.id, other.ownerId, other.currency, other.lockedUntil);
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

    public PositiveAmount unlockedBalance() {
        return locked() ? PositiveAmount.ZERO : balance();
    }

    public PositiveAmount unlockedBalance(final @NonNull Network network) {
        return unlockedBalance();
    }

    public PositiveAmount balance() {
        return PositiveAmount.of(transactions.stream()
                .map(Transaction::amount)
                .reduce(Amount.ZERO, Amount::add));
    }

    public PositiveAmount payableAmount(final @NonNull PositiveAmount amount, final @NonNull Network network) {
        return min(amount, unlockedBalance(network));
    }

    public Transaction credit(PositiveAmount amount, Network network, ZonedDateTime lockedUntil) {
        final var transaction = Transaction.create(network, "null", amount, lockedUntil, "null", "null");
        transactions.add(transaction);
        return transaction;
    }

    public Transaction debit(PositiveAmount amount, Network network) {
        if (unlockedBalance(network).isStrictlyLowerThan(amount))
            throw badRequest("Not enough fund on ledger %s on network %s".formatted(id, network));

        final var transaction = Transaction.create(network, "null", amount.negate(), "null", "null");
        transactions.add(transaction);
        return transaction;
    }

    public Optional<Network> network() {
        return transactions.stream().map(Transaction::network).findFirst();
    }

    public Optional<ZonedDateTime> lockedUntil() {
        return Optional.ofNullable(lockedUntil);
    }

    public boolean locked() {
        return lockedUntil != null && lockedUntil.isAfter(ZonedDateTime.now());
    }

    public void add(Transaction transaction) {
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

    public record Transaction(
            @NonNull Transaction.Id id,
            @NonNull Network network,
            @NonNull String reference,
            @NonNull Amount amount,
            ZonedDateTime lockedUntil,
            @NonNull String thirdPartyName,
            @NonNull String thirdPartyAccountNumber
    ) {
        public static Transaction create(
                final @NonNull Network network,
                final @NonNull String reference,
                final @NonNull Amount amount,
                final @NonNull String thirdPartyName,
                final @NonNull String thirdPartyAccountNumber
        ) {
            return create(network, reference, amount, null, thirdPartyName, thirdPartyAccountNumber);
        }

        public static Transaction create(
                final @NonNull Network network,
                final @NonNull String reference,
                final @NonNull Amount amount,
                final ZonedDateTime lockedUntil,
                final @NonNull String thirdPartyName,
                final @NonNull String thirdPartyAccountNumber
        ) {
            return new Transaction(Id.random(), network, reference, amount, lockedUntil, thirdPartyName, thirdPartyAccountNumber);
        }

        @NoArgsConstructor(staticName = "random")
        @EqualsAndHashCode(callSuper = true)
        @SuperBuilder
        public static class Id extends UuidWrapper {
            public static Transaction.Id of(@NonNull final UUID uuid) {
                return Transaction.Id.builder().uuid(uuid).build();
            }

            public static Transaction.Id of(@NonNull final String uuid) {
                return Transaction.Id.of(UUID.fromString(uuid));
            }
        }
    }
}
