package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileAdminView;
import onlydust.com.marketplace.accounting.domain.view.ShortSponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserBillingProfileTypeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
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
@TypeDef(name = "id_document_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "verification_status", typeClass = PostgreSQLEnumType.class)
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
    @Type(type = "jsonb")
    List<SponsorLinkView> sponsors;
    @Type(type = "jsonb")
    List<String> githubUrls;
    String currencyName;
    String currencyCode;
    String currencyLogoUrl;
    String transactionHash;
    UUID companyId;
    UUID companyOwnerId;
    @Type(type = "verification_status")
    @Enumerated(EnumType.STRING)
    VerificationStatusEntity companyVerificationStatus;
    String companyName;
    String companyNumber;
    Date companyRegistrationDate;
    String companyCountry;
    String companyAddress;
    Boolean companyUsEntity;
    Boolean companySubjectToEuVat;
    String companyEuVatNumber;
    Date companyUpdatedAt;
    String companyApplicantId;
    UUID individualId;
    UUID individualOwnerId;
    @Type(type = "verification_status")
    @Enumerated(EnumType.STRING)
    VerificationStatusEntity individualVerificationStatus;
    String individualFirstName;
    String individualLastName;
    String individualAddress;
    String individualCountry;
    Date individualBirthdate;
    Date individualValidUntil;
    String individualIdDocumentNumber;
    @Type(type = "id_document_type")
    @Enumerated(EnumType.STRING)
    IndividualBillingProfileEntity.IdDocumentTypeEnumEntity individualIdDocumentType;
    String individualIdDocumentCountryCode;
    Boolean individualUsCitizen;
    Date individualUpdatedAt;
    String individualApplicantId;

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
        final ShortBillingProfileAdminView billingProfileAdminView = switch (this.billingProfileType) {
            case INDIVIDUAL -> ShortBillingProfileAdminView.builder()
                    .adminGithubLogin(this.recipientLogin)
                    .adminEmail(this.recipientEmail)
                    .adminName(this.recipientName)
                    .adminGithubAvatarUrl(this.recipientAvatarUrl)
                    .billingProfileId(BillingProfile.Id.of(this.individualId))
                    .billingProfileName(this.individualFirstName + " " + this.individualLastName)
                    .billingProfileType(BillingProfile.Type.INDIVIDUAL)
                    .kyc(Kyc.builder()
                            .id(this.individualId)
                            .ownerId(UserId.of(this.individualOwnerId))
                            .firstName(this.individualFirstName)
                            .lastName(this.individualLastName)
                            .status(this.individualVerificationStatus.toDomain())
                            .country(Country.fromIso3(this.individualCountry))
                            .idDocumentCountryCode(this.individualIdDocumentCountryCode)
                            .address(this.individualAddress)
                            .birthdate(this.individualBirthdate)
                            .usCitizen(this.individualUsCitizen)
                            .validUntil(this.individualValidUntil)
                            .externalApplicantId(this.individualApplicantId)
                            .idDocumentType(isNull(this.individualIdDocumentType) ? null : this.individualIdDocumentType.toDomain())
                            .idDocumentNumber(this.individualIdDocumentNumber)
                            .build())
                    .build();
            case COMPANY -> ShortBillingProfileAdminView.builder()
                    .adminGithubLogin(this.recipientLogin)
                    .adminEmail(this.recipientEmail)
                    .adminName(this.recipientName)
                    .adminGithubAvatarUrl(this.recipientAvatarUrl)
                    .billingProfileId(BillingProfile.Id.of(this.companyId))
                    .billingProfileName(this.companyName)
                    .billingProfileType(BillingProfile.Type.COMPANY)
                    .kyb(
                            Kyb.builder()
                                    .ownerId(UserId.of(this.companyOwnerId))
                                    .id(this.companyId)
                                    .status(this.companyVerificationStatus.toDomain())
                                    .name(this.companyName)
                                    .address(this.companyAddress)
                                    .usEntity(this.companyUsEntity)
                                    .country(Country.fromIso3(this.companyCountry))
                                    .registrationNumber(this.companyNumber)
                                    .registrationDate(this.companyRegistrationDate)
                                    .subjectToEuropeVAT(this.companySubjectToEuVat)
                                    .euVATNumber(this.companyEuVatNumber)
                                    .externalApplicantId(this.companyApplicantId)
                                    .build()
                    )
                    .build();
        };

        return RewardView.builder()
                .id(this.rewardId)
                .requestedAt(this.requestedAt)
                .processedAt(this.processedAt)
                .githubUrls(this.githubUrls)
                .projectName(this.projectName)
                .projectLogoUrl(this.projectLogoUrl)
                .billingProfileAdmin(billingProfileAdminView)
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
                .transactionHash(this.transactionHash)
                .build();
    }
}
