package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.HistoricalTransactionType;
import onlydust.com.backoffice.api.contract.model.MoneyWithUsdEquivalentResponse;
import onlydust.com.backoffice.api.contract.model.TransactionHistoryPageItemResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.api.read.mapper.NetworkMapper;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(schema = "accounting", name = "all_sponsor_account_transactions")
@Immutable
@Accessors(fluent = true)
public class AllSponsorAccountTransactionReadEntity {
    @Id
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
    @JoinColumn(name = "sponsorId")
    @NonNull
    SponsorReadEntity sponsor;

    @ManyToOne
    @JoinColumn(name = "currencyId")
    @NonNull
    CurrencyReadEntity currency;

    @NonNull
    BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "programId")
    ProgramReadEntity program;

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
        final var usdQuote = currency.latestUsdQuote() == null ? null : currency.latestUsdQuote().getPrice();
        return new TransactionHistoryPageItemResponse()
                .date(timestamp)
                .type(type.toBoResponse())
                .network(NetworkMapper.map(network))
                .program(program == null ? null : program.toBoLinkResponse())
                .amount(new MoneyWithUsdEquivalentResponse()
                        .amount(amount)
                        .currency(currency.toBoShortResponse())
                        .conversionRate(usdQuote)
                        .dollarsEquivalent(prettyUsd(usdQuote == null ? null : usdQuote.multiply(amount))));
    }
}
