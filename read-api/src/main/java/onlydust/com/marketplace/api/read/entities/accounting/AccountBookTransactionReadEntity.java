package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.Transaction.Type;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.contract.model.SponsorAccountTransactionType;
import onlydust.com.marketplace.api.contract.model.TransactionHistoryPageItemResponse;
import onlydust.com.marketplace.api.read.entities.billing_profile.BatchPaymentReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

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
    UUID id;

    @NonNull
    Date timestamp;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "transaction_type")
    @NonNull
    Type type;

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
    @JoinColumn(name = "accountBookId")
    AccountBookReadEntity accountBook;

    public static Type map(SponsorAccountTransactionType type) {
        return switch (type) {
            case DEPOSIT -> Type.MINT;
            case WITHDRAWAL -> Type.BURN;
            case ALLOCATION -> Type.TRANSFER;
            case UNALLOCATION -> Type.REFUND;
        };
    }

    public TransactionHistoryPageItemResponse toPageItemResponse() {
        final var usdQuote = accountBook.currency().latestUsdQuote() == null ? null : accountBook.currency().latestUsdQuote().getPrice();
        return new TransactionHistoryPageItemResponse()
                .id(id)
                .date(timestamp.toInstant().atZone(ZoneOffset.UTC))
                .type(map(type))
                .project(project == null ? null : project.toLinkResponse())
                .amount(new Money()
                        .amount(amount)
                        .prettyAmount(pretty(amount, accountBook.currency().decimals(), usdQuote))
                        .currency(accountBook.currency().toShortResponse())
                        .usdEquivalent(prettyUsd(usdQuote == null ? null : usdQuote.multiply(amount)))
                        .usdConversionRate(usdQuote)
                );
    }

    private SponsorAccountTransactionType map(Type type) {
        return switch (type) {
            case MINT -> SponsorAccountTransactionType.DEPOSIT;
            case BURN -> SponsorAccountTransactionType.WITHDRAWAL;
            case TRANSFER -> SponsorAccountTransactionType.ALLOCATION;
            case REFUND -> SponsorAccountTransactionType.UNALLOCATION;
        };
    }
}
