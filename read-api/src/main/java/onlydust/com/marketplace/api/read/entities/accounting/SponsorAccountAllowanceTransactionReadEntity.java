package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.contract.model.SponsorAccountTransactionType;
import onlydust.com.marketplace.api.contract.model.TransactionHistoryPageItemResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
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
@Table(name = "sponsor_account_allowance_transactions", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class SponsorAccountAllowanceTransactionReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    Date timestamp;

    @ManyToOne
    @JoinColumn(name = "accountId")
    @NonNull
    SponsorAccountReadEntity account;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "transaction_type")
    @NonNull
    Type type;

    @NonNull
    BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "projectId")
    ProjectReadEntity project;

    public enum Type {
        MINT, BURN, TRANSFER, REFUND // Allowance transactions
        ;

        public static Type from(SponsorAccountTransactionType type) {
            return switch (type) {
                case DEPOSIT -> MINT;
                case WITHDRAWAL -> BURN;
                case ALLOCATION -> TRANSFER;
                case UNALLOCATION -> REFUND;
            };
        }

        public SponsorAccountTransactionType toResponse() {
            return switch (this) {
                case MINT -> SponsorAccountTransactionType.DEPOSIT;
                case BURN -> SponsorAccountTransactionType.WITHDRAWAL;
                case TRANSFER -> SponsorAccountTransactionType.ALLOCATION;
                case REFUND -> SponsorAccountTransactionType.UNALLOCATION;
            };
        }
    }

    public TransactionHistoryPageItemResponse toResponse() {
        final var usdQuote = account.currency().latestUsdQuote() == null ? null : account.currency().latestUsdQuote().getPrice();
        return new TransactionHistoryPageItemResponse()
                .id(id)
                .date(timestamp.toInstant().atZone(ZoneOffset.UTC))
                .type(type.toResponse())
                .project(project == null ? null : project.toLinkResponse())
                .amount(new Money()
                        .amount(amount)
                        .prettyAmount(pretty(amount, account.currency().decimals(), usdQuote))
                        .currency(account.currency().toShortResponse())
                        .usdEquivalent(usdQuote == null ? null : prettyUsd(usdQuote.multiply(amount)))
                        .usdConversionRate(usdQuote))
                ;
    }
}
