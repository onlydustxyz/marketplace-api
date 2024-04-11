package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@TypeDef(name = "transaction_type", typeClass = PostgreSQLEnumType.class)
@Value
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "sponsor_account_allowance_transactions", schema = "accounting")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SponsorAccountAllowanceTransactionsEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;
    @NonNull UUID accountId;

    @Enumerated(javax.persistence.EnumType.STRING)
    @Type(type = "transaction_type")
    @NonNull TransactionType type;

    @NonNull BigDecimal amount;

    UUID projectId;

    public SponsorAccount.AllowanceTransaction toDomain() {
        return new SponsorAccount.AllowanceTransaction(
                SponsorAccount.Transaction.Id.of(id),
                type.toDomain(),
                Amount.of(amount),
                projectId == null ? null : ProjectId.of(projectId));
    }

    public static SponsorAccountAllowanceTransactionsEntity of(SponsorAccount.Id sponsorAccountId, SponsorAccount.AllowanceTransaction transaction) {
        return SponsorAccountAllowanceTransactionsEntity.builder()
                .id(transaction.id().value())
                .type(TransactionType.of(transaction.type()))
                .accountId(sponsorAccountId.value())
                .amount(transaction.amount().getValue())
                .projectId(transaction.projectId() == null ? null : transaction.projectId().value())
                .build();
    }

    public enum TransactionType {
        ALLOWANCE, ALLOCATION;

        public SponsorAccount.AllowanceTransaction.Type toDomain() {
            return switch (this) {
                case ALLOCATION -> SponsorAccount.AllowanceTransaction.Type.ALLOCATION;
                case ALLOWANCE -> SponsorAccount.AllowanceTransaction.Type.ALLOWANCE;
            };
        }

        public static TransactionType of(SponsorAccount.AllowanceTransaction.Type type) {
            return switch (type) {
                case ALLOWANCE -> ALLOWANCE;
                case ALLOCATION -> ALLOCATION;
            };
        }
    }
}

