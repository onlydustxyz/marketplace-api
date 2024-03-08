package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
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
public class RewardDetailsViewEntity {
    @Id
    @NonNull UUID id;
    @NonNull String status;
    @NonNull Date requestedAt;
    Date processedAt;

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

    @NonNull BigDecimal dollarsEquivalent;
    @NonNull BigDecimal amount;
    String currencyName;
    @NonNull String currencyCode;
    String currencyLogoUrl;

    @Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    BillingProfileEntity.Type billingProfileType;
    String billingProfileName;
    UUID billingProfileId;
    @Type(type = "verification_status")
    @Enumerated(EnumType.STRING)
    VerificationStatusEntity billingProfileVerificationStatus;
    @Type(type = "jsonb")
    ShortBillingProfileAdminView.Admin invoiceCreator;

    UUID invoiceId;
    String invoiceNumber;
    @Type(type = "invoice_status")
    @Enumerated(EnumType.STRING)
    InvoiceEntity.Status invoiceStatus;

    @Type(type = "jsonb")
    List<String> transactionReferences;
    @Type(type = "jsonb")
    List<String> paidToAccountNumbers;

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
                .status(RewardDetailsView.Status.valueOf(this.status))
                .requestedAt(DateMapper.ofNullable(this.requestedAt))
                .processedAt(DateMapper.ofNullable(this.processedAt))
                .githubUrls(isNull(this.githubUrls) ? List.of() : this.githubUrls.stream().sorted().toList())
                .project(ShortProjectView.builder()
                        .id(ProjectId.of(this.projectId))
                        .name(this.projectName)
                        .logoUrl(this.projectLogoUrl)
                        .shortDescription(this.projectShortDescription)
                        .slug(this.projectSlug)
                        .build())
                .billingProfileAdmin(isNull(this.billingProfileId) ? null :
                        ShortBillingProfileAdminView.builder()
                                .admins(List.of(this.invoiceCreator))
                                .billingProfileId(BillingProfile.Id.of(this.billingProfileId))
                                .billingProfileName(this.billingProfileName)
                                .billingProfileType(switch (this.billingProfileType) {
                                    case INDIVIDUAL -> BillingProfile.Type.INDIVIDUAL;
                                    case COMPANY -> BillingProfile.Type.COMPANY;
                                    case SELF_EMPLOYED -> BillingProfile.Type.SELF_EMPLOYED;
                                })
                                .verificationStatus(this.billingProfileVerificationStatus.toDomain())
                                .build())
                .recipient(new ShortContributorView(this.recipientLogin, this.recipientAvatarUrl))
                .sponsors(isNull(this.sponsors) ? List.of() : this.sponsors.stream()
                        .map(SponsorLinkView::toDomain)
                        .sorted(comparing(ShortSponsorView::name))
                        .toList())
                .money(MoneyView.builder()
                        .amount(this.amount)
                        .dollarsEquivalent(this.dollarsEquivalent)
                        .currencyName(this.currencyName)
                        .currencyCode(this.currencyCode)
                        .currencyLogoUrl(this.currencyLogoUrl)
                        .build())
                .invoice(isNull(this.invoiceId) ? null : ShortInvoiceView.builder()
                        .id(Invoice.Id.of(this.invoiceId))
                        .number(Invoice.Number.fromString(this.invoiceNumber))
                        .status(Invoice.Status.valueOf(this.invoiceStatus.toString()))
                        .build())
                .transactionReferences(isNull(this.transactionReferences) ? List.of() : this.transactionReferences)
                .paidToAccountNumbers(isNull(this.paidToAccountNumbers) ? List.of() : this.paidToAccountNumbers)
                .build();
    }
}
