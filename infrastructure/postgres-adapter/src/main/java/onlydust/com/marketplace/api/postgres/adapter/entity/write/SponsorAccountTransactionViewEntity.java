package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@NoArgsConstructor(force = true)
@TypeDef(name = "transaction_type", typeClass = PostgreSQLEnumType.class)
@Table(schema = "accounting", name = "all_sponsor_account_transactions")
public class SponsorAccountTransactionViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;
    @NonNull ZonedDateTime timestamp;

    @Enumerated(javax.persistence.EnumType.STRING)
    @Type(type = "transaction_type")
    @NonNull TransactionType type;

    @ManyToOne
    @NonNull SponsorAccountEntity sponsorAccount;

    @ManyToOne
    @JoinColumn(name = "project_id")
    ProjectEntity project;

    @NonNull BigDecimal amount;

    public enum TransactionType {
        DEPOSIT, SPEND, ALLOWANCE, ALLOCATION;

        public HistoricalTransaction.Type toDomain() {
            return switch (this) {
                case DEPOSIT -> HistoricalTransaction.Type.DEPOSIT;
                case SPEND -> HistoricalTransaction.Type.SPEND;
                case ALLOWANCE -> HistoricalTransaction.Type.ALLOWANCE;
                case ALLOCATION -> HistoricalTransaction.Type.ALLOCATION;
            };
        }
    }

    public HistoricalTransaction toDomain() {
        final var sponsorAccount = this.sponsorAccount.toDomain();

        return new HistoricalTransaction(
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
