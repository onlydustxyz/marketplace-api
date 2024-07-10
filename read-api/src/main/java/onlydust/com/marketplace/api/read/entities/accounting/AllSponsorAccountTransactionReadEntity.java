package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.HistoricalTransactionType;
import onlydust.com.backoffice.api.contract.model.MoneyWithUsdEquivalentResponse;
import onlydust.com.backoffice.api.contract.model.TransactionHistoryPageItemResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.mapper.NetworkMapper;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(schema = "accounting", name = "all_sponsor_account_transactions")
@Immutable
@Accessors(fluent = true)
public class AllSponsorAccountTransactionReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    ZonedDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "transaction_type")
    @NonNull
    Type type;

    @ManyToOne
    @JoinColumn(name = "sponsorAccountId")
    @NonNull
    SponsorAccountReadEntity sponsorAccount;

    @NonNull
    BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "projectId")
    ProjectReadEntity project;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    NetworkEnumEntity network;

    public enum Type {
        DEPOSIT, WITHDRAW, SPEND, // Balance transactions
        MINT, BURN, TRANSFER, REFUND // Allowance transactions
        ;

        public HistoricalTransactionType toBoResponse() {
            return switch (this) {
                case DEPOSIT -> HistoricalTransactionType.DEPOSIT;
                case WITHDRAW -> HistoricalTransactionType.WITHDRAWAL;
                case SPEND -> HistoricalTransactionType.SPEND;
                case MINT -> HistoricalTransactionType.MINT;
                case BURN -> HistoricalTransactionType.BURN;
                case TRANSFER -> HistoricalTransactionType.TRANSFER;
                case REFUND -> HistoricalTransactionType.REFUND;
            };
        }
    }

    public TransactionHistoryPageItemResponse toBoResponse() {
        final var usdQuote = sponsorAccount.currency().latestUsdQuote() == null ? null : sponsorAccount.currency().latestUsdQuote().getPrice();
        return new TransactionHistoryPageItemResponse()
                .date(timestamp)
                .type(type.toBoResponse())
                .network(NetworkMapper.map(network))
                .lockedUntil(sponsorAccount.lockedUntil())
                .project(project == null ? null : project.toBoLinkResponse())
                .amount(new MoneyWithUsdEquivalentResponse()
                        .amount(amount)
                        .currency(sponsorAccount.currency().toBoShortResponse())
                        .conversionRate(usdQuote)
                        .dollarsEquivalent(usdQuote == null ? null : usdQuote.multiply(amount)));
    }
}
