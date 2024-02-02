package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
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
public class SponsorAccount {
    @EqualsAndHashCode.Include
    final @NonNull Id id;
    final @NonNull Object ownerId;
    final @NonNull Currency currency;
    Instant lockedUntil;

    @Getter
    final @NonNull List<Transaction> transactions = new ArrayList<>();

    public <OwnerId> SponsorAccount(final @NonNull OwnerId ownerId, final @NonNull Currency currency, ZonedDateTime lockedUntil) {
        this.id = Id.random();
        this.ownerId = ownerId;
        this.currency = currency;
        this.lockedUntil = lockedUntil == null ? null : lockedUntil.toInstant();
    }

    public <OwnerId> SponsorAccount(final @NonNull OwnerId ownerId, final @NonNull Currency currency) {
        this.id = Id.random();
        this.ownerId = ownerId;
        this.currency = currency;
    }

    public static SponsorAccount of(SponsorAccount other) {
        final var sponsorAccount = new SponsorAccount(other.id, other.ownerId, other.currency, other.lockedUntil);
        sponsorAccount.transactions.addAll(other.transactions);
        return sponsorAccount;
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

    public Amount unlockedBalance() {
        return locked() ? PositiveAmount.ZERO : balance();
    }

    public Amount balance() {
        return transactions.stream()
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

        if (transactions.stream().map(Transaction::reference).anyMatch(transaction.reference()::equals))
            throw badRequest("Transaction with reference %s already exists".formatted(transaction.reference()));

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
            return new Transaction(network, reference, amount, lockedUntil, thirdPartyName, thirdPartyAccountNumber);
        }

        public Transaction withAmount(Amount amount) {
            return new Transaction(network, reference, amount, lockedUntil, thirdPartyName, thirdPartyAccountNumber);
        }
    }
}
