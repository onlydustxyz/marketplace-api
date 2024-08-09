package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.billing_profile.BatchPaymentReadEntity;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "account_book_transactions", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class AccountBookTransactionReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long index;

    @NonNull
    ZonedDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "sponsorAccountId")
    SponsorAccountReadEntity sponsorAccount;

    @ManyToOne
    @JoinColumn(name = "projectId")
    ProjectReadEntity project;

    @ManyToOne
    @JoinColumn(name = "rewardId")
    RewardReadEntity reward;

    @ManyToOne
    @JoinColumn(name = "paymentId")
    BatchPaymentReadEntity payment;

    @NonNull
    BigDecimal amount;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "currencyId")
    CurrencyReadEntity currency;
}
