package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Table(name = "sponsor_accounts", schema = "accounting")
@Builder(access = AccessLevel.PRIVATE)
public class SponsorAccountEntity {
    @Id
    @NonNull UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @NonNull CurrencyEntity currency;

    UUID sponsorId;

    Instant lockedUntil;

    @OneToMany(mappedBy = "accountId", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    Set<SponsorAccountTransactionsEntity> transactions = new HashSet<>();

    public SponsorAccount toDomain() {
        final var ledger = new SponsorAccount(SponsorAccount.Id.of(id), SponsorId.of(sponsorId), currency.toDomain(), lockedUntil);
        ledger.getTransactions().addAll(transactions.stream().map(SponsorAccountTransactionsEntity::toTransaction).toList());
        return ledger;
    }

    public static SponsorAccountEntity of(SponsorAccount sponsorAccount) {
        return SponsorAccountEntity.builder()
                .id(sponsorAccount.id().value())
                .currency(CurrencyEntity.of(sponsorAccount.currency()))
                .lockedUntil(sponsorAccount.lockedUntil().orElse(null))
                .sponsorId(sponsorAccount.sponsorId().value())
                .transactions(sponsorAccount.getTransactions().stream().map(t -> SponsorAccountTransactionsEntity.of(sponsorAccount.id(), t)).collect(Collectors.toUnmodifiableSet()))
                .build();
    }
}

