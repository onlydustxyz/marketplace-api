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
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
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
    @Column(insertable = false, updatable = false)
    UUID rewardId;

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
                .thirdParty(programTransactionThirdParty())
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

    private ProgramTransactionPageItemResponseThirdParty programTransactionThirdParty() {
        return project == null ?
                new ProgramTransactionPageItemResponseThirdParty().sponsor(sponsor.toLinkResponse()) :
                new ProgramTransactionPageItemResponseThirdParty().project(project.toLinkResponse());
    }

    private ProgramTransactionType programTransactionType() {
        return switch (type) {
            case TRANSFER -> project == null ? ProgramTransactionType.ALLOCATED : ProgramTransactionType.GRANTED;
            case REFUND -> project == null ? ProgramTransactionType.UNALLOCATED : ProgramTransactionType.UNGRANTED;
            default -> throw new IllegalStateException("%s transaction types are not allowed for program transactions".formatted(type));
        };
    }

    private SponsorTransactionType sponsorTransactionType() {
        return switch (type) {
            case MINT, DEPOSIT -> SponsorTransactionType.DEPOSITED;
            case TRANSFER -> SponsorTransactionType.ALLOCATED;
            case REFUND -> SponsorTransactionType.UNALLOCATED;
            case BURN, WITHDRAW -> throw new NotImplementedException("%s transaction type is not implemented".formatted(type));
            default -> throw new IllegalStateException("%s transaction types are not allowed for sponsor transactions".formatted(type));
        };
    }

    public void toProgramCsv(CSVPrinter csv) throws IOException {
        final var amount = toMoney(this.amount);
        csv.printRecord(id,
                timestamp,
                programTransactionType().name(),
                programTransactionThirdParty().getProject() == null ? null : programTransactionThirdParty().getProject().getId(),
                programTransactionThirdParty().getSponsor() == null ? null : programTransactionThirdParty().getSponsor().getId(),
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
            case REJECTED -> SponsorDepositTransactionStatus.REJECTED;
            case COMPLETED -> SponsorDepositTransactionStatus.COMPLETED;
            case DRAFT -> throw new IllegalStateException("DRAFT, REJECTED deposit status are not allowed here");
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

    public ProjectTransactionPageItemResponse toProjectTransactionPageItemResponse() {
        return new ProjectTransactionPageItemResponse()
                .id(id)
                .date(timestamp.toInstant().atZone(ZoneOffset.UTC))
                .type(projectTransactionType())
                .thirdParty(projectTransactionThirdParty())
                .amount(toMoney(amount))
                ;
    }

    private ProjectTransactionPageItemResponseThirdParty projectTransactionThirdParty() {
        if (program == null) {
            throw OnlyDustException.internalServerError("Program not found for project transaction %s".formatted(id));
        }
        return reward == null ?
                new ProjectTransactionPageItemResponseThirdParty().program(program.toLinkResponse()) :
                new ProjectTransactionPageItemResponseThirdParty().contributor(reward.recipient().toContributorResponse());
    }

    private ProjectTransactionType projectTransactionType() {
        return switch (type) {
            case TRANSFER -> rewardId == null ? ProjectTransactionType.GRANTED : ProjectTransactionType.REWARDED;
            case REFUND -> ProjectTransactionType.UNGRANTED;
            default -> throw new IllegalStateException("%s transaction types are not allowed for project transactions".formatted(type));
        };
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
