package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileAdminView;
import onlydust.com.marketplace.accounting.domain.view.ShortSponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserBillingProfileTypeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "billing_profile_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class InvoiceRewardViewEntity {
    @Id
    UUID rewardId;
    ZonedDateTime requestedAt;
    String projectName;
    String projectLogoUrl;
    ZonedDateTime processedAt;
    String recipientLogin;
    String recipientAvatarUrl;
    String recipientName;
    String recipientEmail;
    BigDecimal dollarsEquivalent;
    BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Type(type = "currency")
    CurrencyEnumEntity currency;
    @Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    UserBillingProfileTypeEntity.BillingProfileTypeEntity billingProfileType;
    String billingProfileName;
    @Type(type = "jsonb")
    List<SponsorLinkView> sponsors;
    @Type(type = "jsonb")
    List<String> githubUrls;
    String currencyName;
    String currencyCode;
    String currencyLogoUrl;

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

    public RewardView toDomain() {
        return RewardView.builder()
                .id(this.rewardId)
                .requestedAt(this.requestedAt)
                .processedAt(this.processedAt)
                .githubUrls(this.githubUrls)
                .projectName(this.projectName)
                .projectLogoUrl(this.projectLogoUrl)
                .billingProfileAdmin(ShortBillingProfileAdminView.builder()
                        .adminGithubLogin(this.recipientLogin)
                        .adminEmail(this.recipientEmail)
                        .adminName(this.recipientName)
                        .adminGithubAvatarUrl(this.recipientAvatarUrl)
                        .billingProfileName(this.billingProfileName)
                        .billingProfileType(switch (this.billingProfileType) {
                            case INDIVIDUAL -> BillingProfile.Type.INDIVIDUAL;
                            case COMPANY -> BillingProfile.Type.COMPANY;
                        })
                        .build())
                .sponsors(isNull(this.sponsors) ? List.of() : this.sponsors.stream()
                        .map(SponsorLinkView::toDomain)
                        .toList())
                .money(MoneyView.builder()
                        .amount(this.amount)
                        .dollarsEquivalent(this.dollarsEquivalent)
                        .currencyName(this.currencyName)
                        .currencyCode(this.currencyCode)
                        .currencyLogoUrl(this.currencyLogoUrl)
                        .build())
                .build();
    }
}
