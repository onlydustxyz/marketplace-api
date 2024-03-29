package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class SponsorAccount {
    @EqualsAndHashCode.Include
    final @NonNull Id id;
    final @NonNull SponsorId sponsorId;
    final @NonNull Currency currency;
    Instant lockedUntil;

    @Getter
    final @NonNull List<Transaction> transactions = new ArrayList<>();

    public SponsorAccount(final @NonNull SponsorId sponsorId, final @NonNull Currency currency, ZonedDateTime lockedUntil) {
        this.id = Id.random();
        this.sponsorId = sponsorId;
        this.currency = currency;
        this.lockedUntil = lockedUntil == null ? null : lockedUntil.toInstant();
    }

    public SponsorAccount(final @NonNull SponsorId sponsorId, final @NonNull Currency currency) {
        this.id = Id.random();
        this.sponsorId = sponsorId;
        this.currency = currency;
    }

    public static SponsorAccount of(SponsorAccount other) {
        final var sponsorAccount = new SponsorAccount(other.id, other.sponsorId, other.currency, other.lockedUntil);
        sponsorAccount.transactions.addAll(other.transactions);
        return sponsorAccount;
    }

    public Id id() {
        return this.id;
    }

    public SponsorId sponsorId() {
        return this.sponsorId;
    }

    public Currency currency() {
        return this.currency;
    }

    public Amount unlockedBalance() {
        return locked() ? Amount.ZERO : balance();
    }

    public Amount balance() {
        return transactions.stream()
                .map(Transaction::amount)
                .reduce(Amount.ZERO, Amount::add);
    }

    public Amount initialBalance() {
        return transactions.stream()
                .filter(transaction -> transaction.type() == Transaction.Type.DEPOSIT)
                .map(Transaction::amount)
                .reduce(Amount.ZERO, Amount::add);
    }

    public Optional<Network> network() {
        return transactions.stream().map(Transaction::network).findFirst();
    }

    public Optional<Instant> lockedUntil() {
        return Optional.ofNullable(lockedUntil);
    }

    public boolean locked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public void add(Transaction transaction) {
        if (transaction.amount.isNegative() && locked())
            throw badRequest("Cannot spend from locked account %s".formatted(id));

        if (transaction.amount.add(balance()).isNegative())
            throw badRequest("Not enough fund on account %s".formatted(id));

        if (network().orElse(transaction.network()) != transaction.network())
            throw badRequest("Cannot mix transactions from different networks");

        currency.forNetwork(transaction.network());

        transactions.add(transaction);
    }

    public void lockUntil(ZonedDateTime lockedUntil) {
        this.lockedUntil = lockedUntil != null ? lockedUntil.toInstant() : null;
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

    @Accessors(fluent = true)
    @Getter
    public static class Transaction extends Payment.Reference {
        private final @NonNull Amount amount;
        private final @NonNull Id id;
        final @NonNull Type type;

        public Transaction(
                final @NonNull Type type,
                final @NonNull Payment.Reference paymentReference,
                final @NonNull Amount amount
        ) {
            this(type, paymentReference.network(), paymentReference.reference(), amount, paymentReference.thirdPartyName(),
                    paymentReference.thirdPartyAccountNumber());
        }

        public Transaction(
                final @NonNull Type type,
                final @NonNull Network network,
                final @NonNull String reference,
                final @NonNull Amount amount,
                final @NonNull String thirdPartyName,
                final @NonNull String thirdPartyAccountNumber
        ) {
            this(Id.random(), type, network, reference, amount, thirdPartyName, thirdPartyAccountNumber);
        }

        public Transaction(
                final @NonNull Id id,
                final @NonNull Type type,
                final @NonNull Network network,
                final @NonNull String reference,
                final @NonNull Amount amount,
                final @NonNull String thirdPartyName,
                final @NonNull String thirdPartyAccountNumber
        ) {
            super(network, reference, thirdPartyName, thirdPartyAccountNumber);
            this.amount = amount;
            this.id = id;
            this.type = type;
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

        public enum Type {
            DEPOSIT, // Money received/refunded from the sponsor
            SPEND // Money spent to pay rewards
        }
    }
}
