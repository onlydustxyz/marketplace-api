package onlydust.com.marketplace.accounting.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

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
        return initialBalance().subtract(total(Transaction.Type.SPEND));
    }

    public Amount initialBalance() {
        return total(Transaction.Type.DEPOSIT).subtract(total(Transaction.Type.WITHDRAW));
    }

    private PositiveAmount total(Transaction.Type... types) {
        return transactions.stream()
                .filter(transaction -> List.of(types).contains(transaction.type))
                .map(Transaction::amount)
                .reduce(PositiveAmount.ZERO, PositiveAmount::add);
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

        @JsonCreator
        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    }

    @Accessors(fluent = true)
    @Getter
    public static class Transaction extends Payment.Reference {
        private final @NonNull PositiveAmount amount;
        private final @NonNull Id id;
        final @NonNull Type type;

        public Transaction(
                final @NonNull Type type,
                final @NonNull Payment.Reference paymentReference,
                final @NonNull PositiveAmount amount
        ) {
            this(paymentReference.timestamp(), type, paymentReference.network(), paymentReference.reference(), amount, paymentReference.thirdPartyName(),
                    paymentReference.thirdPartyAccountNumber());
        }

        public Transaction(
                final @NonNull ZonedDateTime timestamp,
                final @NonNull Type type,
                final @NonNull Network network,
                final @NonNull String reference,
                final @NonNull PositiveAmount amount,
                final @NonNull String thirdPartyName,
                final @NonNull String thirdPartyAccountNumber
        ) {
            this(Id.random(), timestamp, type, network, reference, amount, thirdPartyName, thirdPartyAccountNumber);
        }

        public Transaction(
                final @NonNull Id id,
                final @NonNull ZonedDateTime timestamp,
                final @NonNull Type type,
                final @NonNull Network network,
                final @NonNull String reference,
                final @NonNull PositiveAmount amount,
                final @NonNull String thirdPartyName,
                final @NonNull String thirdPartyAccountNumber
        ) {
            super(timestamp, network, reference, thirdPartyName, thirdPartyAccountNumber);
            this.amount = amount;
            this.id = id;
            this.type = type;
        }

        public static @NonNull Transaction deposit(final @NonNull SponsorView sponsor,
                                                   final @NonNull Blockchain.TransferTransaction transaction) {
            return new Transaction(
                    transaction.timestamp(),
                    Type.DEPOSIT,
                    Network.fromBlockchain(transaction.blockchain()),
                    transaction.reference(),
                    PositiveAmount.of(transaction.amount()),
                    transaction.senderAddress(),
                    sponsor.name());
        }

        @NoArgsConstructor(staticName = "random")
        @EqualsAndHashCode(callSuper = true)
        @SuperBuilder
        public static class Id extends UuidWrapper {
            public static Id of(@NonNull final UUID uuid) {
                return Id.builder().uuid(uuid).build();
            }

            @JsonCreator
            public static Id of(@NonNull final String uuid) {
                return Id.of(UUID.fromString(uuid));
            }
        }

        public enum Type {
            DEPOSIT, // Money received from the sponsor
            WITHDRAW, // Money refunded to the sponsor
            SPEND; // Money spent to pay rewards

            public boolean isDebit() {
                return List.of(WITHDRAW, SPEND).contains(this);
            }
        }
    }
}
