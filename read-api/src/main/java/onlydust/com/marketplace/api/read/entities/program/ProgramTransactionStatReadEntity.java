package onlydust.com.marketplace.api.read.entities.program;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.read.entities.accounting.AccountBookReadEntity;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@IdClass(ProgramTransactionStatReadEntity.PrimaryKey.class)
@Table(name = "account_book_transactions", schema = "accounting")
@AllArgsConstructor // TODO remove when migrated to programs
public class ProgramTransactionStatReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID sponsorAccountId;

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID accountBookId;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "accountBookId", insertable = false, updatable = false)
    AccountBookReadEntity accountBook;

    @NonNull
    @Formula("""
            coalesce(sum(amount) filter ( where type in ('MINT', 'TRANSFER') and project_id is null ), 0)
                    - coalesce(sum(amount) filter ( where type in ('REFUND', 'BURN') and project_id is null ), 0)
                    - coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is null ), 0)
                    + coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is null ), 0)
            """)
    BigDecimal totalAvailable;

    @NonNull
    @Formula("""
            coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is null ), 0)
                    - coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is null ), 0)
            """)
    BigDecimal totalGranted;

    @NonNull
    @Formula("""
            coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is not null and payment_id is null ), 0)
                    - coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is not null and payment_id is null ), 0)
            """)
    BigDecimal totalRewarded;

    public Money toMoney(BigDecimal amount) {
        final var usdQuote = accountBook.currency().latestUsdQuote() == null ? null : accountBook.currency().latestUsdQuote().getPrice();

        return new Money()
                .amount(amount)
                .currency(accountBook.currency().toShortResponse())
                .prettyAmount(pretty(amount, accountBook.currency().decimals(), usdQuote))
                .usdEquivalent(prettyUsd(usdQuote == null ? null : usdQuote.multiply(amount)))
                .usdConversionRate(usdQuote);
    }

    public @Nullable BigDecimal usdAmount(BigDecimal amount) {
        return accountBook.currency().latestUsdQuote() == null ? null : accountBook.currency().latestUsdQuote().getPrice().multiply(amount);
    }

    public static ProgramTransactionStatReadEntity merge(ProgramTransactionStatReadEntity left,
                                                         @NonNull ProgramTransactionStatReadEntity right) {
        return left == null ? right : new ProgramTransactionStatReadEntity(
                left.sponsorAccountId(),
                left.accountBookId(),
                left.accountBook(),
                left.totalAvailable().add(right.totalAvailable()),
                left.totalGranted().add(right.totalGranted()),
                left.totalRewarded().add(right.totalRewarded())
        );

    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID sponsorAccountId;
        UUID accountBookId;
    }
}
