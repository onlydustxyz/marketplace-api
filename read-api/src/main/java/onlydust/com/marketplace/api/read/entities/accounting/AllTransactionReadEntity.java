package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.billing_profile.BatchPaymentReadEntity;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "all_transactions", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class AllTransactionReadEntity {
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

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "deposit_status")
    Deposit.Status depositStatus;

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
            case DEPOSIT, SPEND, WITHDRAW ->
                    throw new IllegalStateException("DEPOSIT, SPEND, WITHDRAW transaction types are not allowed for program transactions");
        };
    }

    private SponsorTransactionType sponsorTransactionType() {
        return switch (type) {
            case MINT, DEPOSIT -> SponsorTransactionType.DEPOSITED;
            case TRANSFER -> SponsorTransactionType.ALLOCATED;
            case REFUND -> SponsorTransactionType.RETURNED;
            case WITHDRAW -> throw new NotImplementedException("WITHDRAW transaction type is not implemented");
            case BURN, SPEND -> throw new IllegalStateException("BURN, SPEND transaction types are not allowed for program transactions");
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
        return isNull(depositStatus) ? null : switch (depositStatus) {
            case PENDING -> SponsorDepositTransactionStatus.PENDING;
            case COMPLETED -> SponsorDepositTransactionStatus.COMPLETED;
            case DRAFT, REJECTED -> throw new IllegalStateException("DRAFT, REJECTED deposit status are not allowed here");
        };
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

    public enum Type {
        /**
         * Deposit made by a sponsor
         */
        DEPOSIT,
        /**
         * Withdrawal made by a sponsor (opposite of DEPOSIT)
         */
        WITHDRAW,
        SPEND,
        MINT,
        BURN,
        TRANSFER,
        REFUND
    }
}
