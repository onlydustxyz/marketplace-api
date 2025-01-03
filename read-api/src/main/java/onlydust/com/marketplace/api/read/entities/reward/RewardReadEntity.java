package onlydust.com.marketplace.api.read.entities.reward;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.MoneyWithUsdEquivalentResponse;
import onlydust.com.backoffice.api.contract.model.RewardPageItemResponse;
import onlydust.com.backoffice.api.contract.model.ShortRewardResponse;
import onlydust.com.backoffice.api.contract.model.TotalMoneyWithUsdEquivalentResponse;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Table(name = "rewards", schema = "public")
@Accessors(fluent = true)
@Immutable
public class RewardReadEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    BigDecimal amount;
    @NonNull
    Date requestedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requestorId", referencedColumnName = "userId")
    @NonNull
    AllUserReadEntity requestor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipientId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    @NonNull
    AllUserReadEntity recipient;

    @NonNull
    Long recipientId;

    UUID billingProfileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currencyId")
    @NonNull
    CurrencyReadEntity currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projectId")
    @NonNull
    ProjectLinkReadEntity project;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusReadEntity status;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusDataReadEntity statusData;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceId")
    @Getter(AccessLevel.NONE)
    InvoiceReadEntity invoice;

    public Optional<InvoiceReadEntity> invoice() {
        return Optional.ofNullable(invoice).filter(i -> i.status() != Invoice.Status.DRAFT);
    }

    public BigDecimal usdEquivalent() {
        return statusData.amountUsdEquivalent();
    }

    public BigDecimal usdEquivalentAfterTax() {
        return (BigDecimal.ONE.add(taxRate())).multiply(statusData.amountUsdEquivalent());
    }

    private BigDecimal taxRate() {
        return invoice == null ? BigDecimal.ZERO : invoice.data().taxRate();
    }

    public ShortRewardResponse toShortResponse() {
        return new ShortRewardResponse()
                .id(id)
                .status(status.toBoContract())
                .project(project.toBoLinkResponse())
                .money(new MoneyWithUsdEquivalentResponse()
                        .amount(amount)
                        .currency(currency.toBoShortResponse())
                        .conversionRate(statusData.usdConversionRate())
                        .dollarsEquivalent(statusData.amountUsdEquivalent())
                );
    }

    public TotalMoneyWithUsdEquivalentResponse toTotalMoneyWithUsdEquivalentResponse() {
        return new TotalMoneyWithUsdEquivalentResponse()
                .amount(amount)
                .currency(currency.toBoShortResponse())
                .dollarsEquivalent(prettyUsd(statusData.amountUsdEquivalent()));
    }

    public RewardPageItemResponse toBoPageItemResponse() {
        return new RewardPageItemResponse()
                .id(id)
                .project(project.toBoLinkResponse())
                .status(status.toBoContract())
                .money(new MoneyWithUsdEquivalentResponse()
                        .amount(amount)
                        .currency(currency.toBoShortResponse())
                        .conversionRate(statusData.usdConversionRate())
                        .dollarsEquivalent(statusData.amountUsdEquivalent())
                )
                .recipient(recipient.toBoLinkResponse())
                .billingProfile(invoice().map(i -> i.billingProfile().toBoLinkResponse()).orElse(null))
                .requestedAt(DateMapper.ofNullable(requestedAt))
                .invoice(invoice().map(InvoiceReadEntity::toBoLinkResponse).orElse(null))
                ;
    }
}
