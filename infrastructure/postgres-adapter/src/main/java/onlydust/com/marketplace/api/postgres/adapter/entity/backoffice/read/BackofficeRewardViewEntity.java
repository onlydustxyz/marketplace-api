package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.AllUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.InvoiceViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
public class BackofficeRewardViewEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    Date requestedAt;

    @ManyToOne
    @JoinColumn(name = "recipientId", referencedColumnName = "githubUserId")
    AllUserViewEntity recipient;

    @ManyToOne
    @JoinColumn(name = "requestorId", referencedColumnName = "userId")
    AllUserViewEntity requester;

    @JdbcTypeCode(SqlTypes.JSON)
    List<String> githubUrls;

    @NonNull
    UUID projectId;
    @NonNull
    String projectName;
    String projectLogoUrl;
    @NonNull
    String projectShortDescription;
    @NonNull
    String projectSlug;

    @JdbcTypeCode(SqlTypes.JSON)
    List<SponsorLinkView> sponsors;

    @NonNull
    BigDecimal amount;
    @ManyToOne
    CurrencyEntity currency;

    @ManyToOne
    BillingProfileEntity billingProfile;

    @ManyToOne
    InvoiceViewEntity invoice;

    UUID batchPaymentId;

    @OneToMany
    @JoinTable(schema = "accounting", name = "rewards_receipts",
            joinColumns = @JoinColumn(name = "reward_id"),
            inverseJoinColumns = @JoinColumn(name = "receipt_id"))
    List<ReceiptEntity> receipts;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusEntity status;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull
    RewardStatusDataEntity statusData;

    @Data
    public static class SponsorLinkView {
        String name;
        String logoUrl;

        public ShortSponsorView toDomain() {
            return ShortSponsorView.builder()
                    .logoUrl(this.logoUrl)
                    .name(this.name)
                    .build();
        }
    }

    public RewardDetailsView toDomain() {
        return RewardDetailsView.builder()
                .id(RewardId.of(this.id))
                .paymentId(batchPaymentId == null ? null : Payment.Id.of(batchPaymentId))
                .status(status())
                .requestedAt(DateMapper.ofNullable(this.requestedAt))
                .processedAt(DateMapper.ofNullable(this.statusData.paidAt()))
                .githubUrls(isNull(this.githubUrls) ? List.of() : this.githubUrls.stream().filter(Objects::nonNull).sorted().toList())
                .project(ProjectShortView.builder()
                        .id(ProjectId.of(this.projectId))
                        .name(this.projectName)
                        .logoUrl(this.projectLogoUrl)
                        .shortDescription(this.projectShortDescription)
                        .slug(this.projectSlug)
                        .build())
                .billingProfile(isNull(this.billingProfile) ? null : this.billingProfile.toDomain())
                .recipient(new ShortContributorView(recipient.login(), recipient.avatarUrl(), recipient.email()))
                .requester(new ShortContributorView(requester.login(), requester.avatarUrl(), requester.email()))
                .sponsors(isNull(this.sponsors) ? List.of() : this.sponsors.stream()
                        .map(SponsorLinkView::toDomain)
                        .sorted(comparing(ShortSponsorView::name))
                        .toList())
                .money(new MoneyView(this.amount, this.currency.toDomain(), this.statusData.usdConversionRate(), this.statusData.amountUsdEquivalent()))
                .invoice(isNull(this.invoice) ? null : invoice.toView())
                .receipts(isNull(this.receipts) ? List.of() : this.receipts.stream().map(r -> r.toDomain(RewardId.of(this.id))).toList())
                .build();
    }

    private RewardStatus status() {
        return RewardStatus.builder()
                .projectId(projectId)
                .billingProfileId(billingProfile == null ? null : billingProfile.getId())
                .recipientId(recipient.githubUserId())
                .status(this.status.toDomain())
                .build();
    }
}
