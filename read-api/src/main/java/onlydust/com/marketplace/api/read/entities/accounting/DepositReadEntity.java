package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.BoDepositBillingInformation;
import onlydust.com.backoffice.api.contract.model.BoDepositResponse;
import onlydust.com.backoffice.api.contract.model.DepositPageItemResponse;
import onlydust.com.backoffice.api.contract.model.DepositStatus;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.api.contract.model.DepositBillingInformation;
import onlydust.com.marketplace.api.contract.model.DepositResponse;
import onlydust.com.marketplace.api.contract.model.DepositSenderInformation;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorStatPerCurrencyReadEntity;
import onlydust.com.marketplace.kernel.model.blockchain.MetaBlockExplorer;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

import static onlydust.com.marketplace.api.read.mapper.MoneyMapper.toMoney;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "deposits", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class DepositReadEntity {
    @Id
    @NonNull
    UUID id;

    @NonNull
    @OneToOne(optional = false)
    TransferTransactionReadEntity transaction;

    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "sponsorId")
    SponsorReadEntity sponsor;

    @NonNull
    @ManyToOne(optional = false)
    CurrencyReadEntity currency;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NonNull
    onlydust.com.marketplace.api.contract.model.DepositStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    Deposit.BillingInformation billingInformation;

    public DepositPageItemResponse toBoPageItemResponse(final MetaBlockExplorer blockExplorer) {
        return new DepositPageItemResponse()
                .id(id)
                .sponsor(sponsor.toBoResponse())
                .transaction(transaction.toBoResponse(blockExplorer))
                .currency(currency.toBoShortResponse())
                .status(map(status));
    }

    public BoDepositResponse toBoResponse(final MetaBlockExplorer blockExplorer) {
        return new BoDepositResponse()
                .id(id)
                .sponsor(sponsor.toBoResponse())
                .transaction(transaction.toBoResponse(blockExplorer))
                .currency(currency.toBoShortResponse())
                .status(map(status))
                .billingInformation(billingInformation == null ? null : new BoDepositBillingInformation()
                        .companyName(billingInformation.companyName())
                        .companyAddress(billingInformation.companyAddress())
                        .companyCountry(billingInformation.companyCountry())
                        .companyId(billingInformation.companyId())
                        .vatNumber(billingInformation.vatNumber())
                        .billingEmail(billingInformation.billingEmail())
                        .firstName(billingInformation.firstName())
                        .lastName(billingInformation.lastName())
                        .email(billingInformation.email())
                );
    }

    public DepositResponse toResponse() {
        final var currentBalance = sponsor.statsPerCurrency().stream()
                .filter(stat -> stat.currency().id().equals(currency.id()))
                .map(SponsorStatPerCurrencyReadEntity::totalAvailable)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DepositResponse()
                .id(id)
                .amount(toMoney(transaction.amount(), currency))
                .status(status)
                .currentBalance(toMoney(currentBalance, currency))
                .finalBalance(toMoney(currentBalance.add(transaction.amount()), currency))
                .senderInformation(new DepositSenderInformation()
                        .name(sponsor.name())
                        .accountNumber(transaction.senderAddress())
                        .transactionReference(transaction.reference()))
                .billingInformation(billingInformation == null ? null : new DepositBillingInformation()
                        .companyName(billingInformation.companyName())
                        .companyAddress(billingInformation.companyAddress())
                        .companyCountry(billingInformation.companyCountry())
                        .companyId(billingInformation.companyId())
                        .vatNumber(billingInformation.vatNumber())
                        .billingEmail(billingInformation.billingEmail())
                        .firstName(billingInformation.firstName())
                        .lastName(billingInformation.lastName())
                        .email(billingInformation.email()));
    }

    private DepositStatus map(onlydust.com.marketplace.api.contract.model.DepositStatus status) {
        return switch (status) {
            case DRAFT -> null;
            case PENDING -> DepositStatus.PENDING;
            case COMPLETED -> DepositStatus.COMPLETED;
            case REJECTED -> DepositStatus.REJECTED;
        };
    }
}
