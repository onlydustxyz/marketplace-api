package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@NoArgsConstructor(force = true)
@Table(schema = "accounting", name = "all_sponsor_account_transactions")
public class SponsorAccountTransactionViewEntity {
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
    TransactionType type;

    @ManyToOne
    @NonNull
    SponsorAccountEntity sponsorAccount;

    @ManyToOne
    @JoinColumn(name = "project_id")
    ProjectEntity project;

    @NonNull
    BigDecimal amount;

    public enum TransactionType {
        DEPOSIT, WITHDRAW, SPEND, MINT, BURN, TRANSFER, REFUND;

        public HistoricalTransaction.Type toDomain() {
            return switch (this) {
                case DEPOSIT -> HistoricalTransaction.Type.DEPOSIT;
                case WITHDRAW -> HistoricalTransaction.Type.WITHDRAW;
                case SPEND -> HistoricalTransaction.Type.SPEND;
                case MINT -> HistoricalTransaction.Type.MINT;
                case BURN -> HistoricalTransaction.Type.BURN;
                case TRANSFER -> HistoricalTransaction.Type.TRANSFER;
                case REFUND -> HistoricalTransaction.Type.REFUND;
            };
        }

        public static TransactionType of(HistoricalTransaction.Type type) {
            return switch (type) {
                case DEPOSIT -> DEPOSIT;
                case WITHDRAW -> WITHDRAW;
                case SPEND -> SPEND;
                case MINT -> MINT;
                case BURN -> BURN;
                case TRANSFER -> TRANSFER;
                case REFUND -> REFUND;
            };
        }
    }

    public HistoricalTransaction toDomain() {
        final var sponsorAccount = this.sponsorAccount.toDomain();

        return new HistoricalTransaction(
                id,
                timestamp,
                type.toDomain(),
                sponsorAccount,
                Amount.of(amount),
                sponsorAccount.currency().latestUsdQuote()
                        .map(usdConversionRate -> new ConvertedAmount(Amount.of(amount.multiply(usdConversionRate)), usdConversionRate))
                        .orElse(null),
                project == null ? null : ShortProjectView.builder()
                        .id(ProjectId.of(project.getId()))
                        .name(project.getName())
                        .logoUrl(project.getLogoUrl())
                        .slug(project.getKey())
                        .shortDescription(project.getShortDescription())
                        .build()
        );
    }
}
