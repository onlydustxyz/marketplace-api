package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.Transaction.Type;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.billing_profile.BatchPaymentReadEntity;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "account_book_transactions", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class AccountBookTransactionReadEntity {
    @Id
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
    @JoinColumn(name = "sponsorId")
    @NonNull
    SponsorReadEntity sponsor;

    @ManyToOne
    @JoinColumn(name = "programId")
    ProgramReadEntity program;

    @ManyToOne
    @JoinColumn(name = "projectId")
    ProjectLinkReadEntity project;

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

    public static Type map(SponsorAccountTransactionType type) {
        return switch (type) {
            case DEPOSIT -> Type.MINT;
            case WITHDRAWAL -> Type.BURN;
            case ALLOCATION -> Type.TRANSFER;
            case UNALLOCATION -> Type.REFUND;
        };
    }

    public ProgramTransactionPageItemResponse toProgramTransactionPageItemResponse() {
        return new ProgramTransactionPageItemResponse()
                .id(id)
                .date(timestamp.toInstant().atZone(ZoneOffset.UTC))
                .type(programTransactionType())
                .thirdParty(thirdParty())
                .amount(toMoney(amount))
                ;
    }

    private Money toMoney(@NonNull BigDecimal amount) {
        final var usdQuote = currency.latestUsdQuote() == null ? null : currency.latestUsdQuote().getPrice();

        return new Money()
                .amount(amount)
                .currency(currency.toShortResponse())
                .prettyAmount(pretty(amount, currency.decimals(), usdQuote))
                .usdEquivalent(prettyUsd(usdQuote == null ? null : usdQuote.multiply(amount)))
                .usdConversionRate(usdQuote);
    }

    private ProgramTransactionPageItemResponseThirdParty thirdParty() {
        return project == null ?
                new ProgramTransactionPageItemResponseThirdParty().sponsor(sponsor.toLinkResponse()) :
                new ProgramTransactionPageItemResponseThirdParty().project(project().toLinkResponse());
    }

    private ProgramTransactionType programTransactionType() {
        return switch (type) {
            case MINT, TRANSFER -> project == null ? ProgramTransactionType.RECEIVED : ProgramTransactionType.GRANTED;
            case REFUND, BURN -> project == null ? ProgramTransactionType.RETURNED : ProgramTransactionType.GRANTED;
        };
    }

    private SponsorTransactionType sponsorTransactionType() {
        return switch (type) {
            case MINT -> SponsorTransactionType.DEPOSITED;
            case BURN -> throw new IllegalStateException("BURN transaction type is not allowed for sponsor transactions");
            case TRANSFER -> SponsorTransactionType.ALLOCATED;
            case REFUND -> SponsorTransactionType.RETURNED;
        };
    }

    public void toProgramCsv(CSVPrinter csv) throws IOException {
        final var amount = toMoney(this.amount);
        csv.printRecord(id,
                timestamp,
                programTransactionType().name(),
                thirdParty().getProject() == null ? null : thirdParty().getProject().getId(),
                thirdParty().getSponsor() == null ? null : thirdParty().getSponsor().getId(),
                amount.getAmount(),
                amount.getCurrency().getCode(),
                amount.getUsdEquivalent()
        );
    }

    public SponsorTransactionPageItemResponse toSponsorTransactionPageItemResponse() {
        return new SponsorTransactionPageItemResponse()
                .id(id)
                .date(timestamp.toInstant().atZone(ZoneOffset.UTC))
                .type(sponsorTransactionType())
                .program(program == null ? null : program.toLinkResponse())
                .amount(toMoney(amount))
                .depositStatus(depositStatus())
                ;
    }

    private SponsorDepositTransactionStatus depositStatus() {
        return type == Type.MINT ? SponsorDepositTransactionStatus.COMPLETED : null; // TODO
    }

    public void toSponsorCsv(CSVPrinter csv) throws IOException {
        final var amount = toMoney(this.amount);
        csv.printRecord(id,
                timestamp,
                sponsorTransactionType().name(),
                depositStatus(),
                program == null ? null : program.id(),
                amount.getAmount(),
                amount.getCurrency().getCode(),
                amount.getUsdEquivalent()
        );
    }
}
