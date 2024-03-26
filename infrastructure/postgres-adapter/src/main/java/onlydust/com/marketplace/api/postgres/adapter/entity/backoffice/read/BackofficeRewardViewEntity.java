package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "billing_profile_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "invoice_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "verification_status", typeClass = PostgreSQLEnumType.class)
public class BackofficeRewardViewEntity {
    @Id
    @NonNull UUID id;
    @NonNull Date requestedAt;

    String recipientLogin;
    String recipientAvatarUrl;

    @Type(type = "jsonb")
    List<String> githubUrls;

    @NonNull UUID projectId;
    @NonNull String projectName;
    String projectLogoUrl;
    @NonNull String projectShortDescription;
    @NonNull String projectSlug;

    @Type(type = "jsonb")
    List<SponsorLinkView> sponsors;

    @NonNull BigDecimal amount;
    @ManyToOne
    CurrencyEntity currency;

    @ManyToOne
    BillingProfileEntity billingProfile;

    @ManyToOne
    InvoiceEntity invoice;

    @Type(type = "jsonb")
    List<String> transactionReferences;
    @Type(type = "jsonb")
    List<String> paidToAccountNumbers;

    UUID batchPaymentId;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull RewardStatusEntity status;
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "reward_id")
    @NonNull RewardStatusDataEntity statusData;

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

    public BackofficeRewardView toDomain() {
        return BackofficeRewardView.builder()
                .id(RewardId.of(this.id))
                .paymentId(batchPaymentId == null ? null : Payment.Id.of(batchPaymentId))
                .status(status.toDomain())
                .requestedAt(DateMapper.ofNullable(this.requestedAt))
                .processedAt(DateMapper.ofNullable(this.statusData.paidAt()))
                .githubUrls(isNull(this.githubUrls) ? List.of() : this.githubUrls.stream().sorted().toList())
                .project(ShortProjectView.builder()
                        .id(ProjectId.of(this.projectId))
                        .name(this.projectName)
                        .logoUrl(this.projectLogoUrl)
                        .shortDescription(this.projectShortDescription)
                        .slug(this.projectSlug)
                        .build())
                .billingProfile(isNull(this.billingProfile) ? null : this.billingProfile.toDomain())
                .recipient(new ShortContributorView(this.recipientLogin, this.recipientAvatarUrl))
                .sponsors(isNull(this.sponsors) ? List.of() : this.sponsors.stream()
                        .map(SponsorLinkView::toDomain)
                        .sorted(comparing(ShortSponsorView::name))
                        .toList())
                .money(new MoneyView(this.amount, this.currency.toDomain(), this.statusData.usdConversionRate(), this.statusData.amountUsdEquivalent()))
                .invoice(isNull(this.invoice) ? null : invoice.toShortView())
                .transactionReferences(isNull(this.transactionReferences) ? List.of() : this.transactionReferences)
                .paidToAccountNumbers(isNull(this.paidToAccountNumbers) ? List.of() : this.paidToAccountNumbers)
                .build();
    }
}
