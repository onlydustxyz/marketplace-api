package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.AccountResponse;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsor_accounts", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class SponsorAccountReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @OneToOne
    @NonNull
    CurrencyReadEntity currency;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sponsorId")
    @NonNull
    SponsorReadEntity sponsor;

    ZonedDateTime lockedUntil;

    @OneToMany(mappedBy = "account")
    @OrderBy("timestamp DESC")
    @NonNull
    List<SponsorAccountTransactionReadEntity> transactions;

    public AccountResponse toDto(SponsorAccountStatement sponsorAccountStatement) {
        return new AccountResponse()
                .id(id)
                .sponsorId(sponsor.id())
                .lockedUntil(lockedUntil)
                .receipts(transactions.stream().map(SponsorAccountTransactionReadEntity::toDto).toList())
                .currency(currency.toBoShortResponse())
                .initialBalance(sponsorAccountStatement.account().initialBalance().getValue())
                .currentBalance(sponsorAccountStatement.account().balance().getValue())
                .initialAllowance(sponsorAccountStatement.initialAllowance().getValue())
                .currentAllowance(sponsorAccountStatement.allowance().getValue())
                .debt(sponsorAccountStatement.debt().getValue())
                .awaitingPaymentAmount(sponsorAccountStatement.awaitingPaymentAmount().getValue())
                ;
    }
}
