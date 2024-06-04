package onlydust.com.marketplace.bff.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.backoffice.api.contract.model.BillingProfileShortResponse;
import onlydust.com.backoffice.api.contract.model.BillingProfileType;
import onlydust.com.backoffice.api.contract.model.VerificationStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Table(name = "billing_profiles", schema = "accounting")
public class BillingProfileReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;

    @NonNull String name;
    @Enumerated(EnumType.STRING)
    @NonNull BillingProfileType type;

    ZonedDateTime invoiceMandateAcceptedAt;

    @Enumerated(EnumType.STRING)
    @NonNull VerificationStatus verificationStatus;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    KycReadEntity kyc;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    KybReadEntity kyb;

    Boolean enabled;

    @OneToMany(mappedBy = "billingProfile")
    Set<AllBillingProfileUserReadEntity> users;

    @OneToMany(mappedBy = "billingProfile")
    Set<BillingProfileUserInvitationReadEntity> invitations;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    BillingProfileStatsReadEntity stats;

//    @OneToOne
//    @JoinFormula(value = "1")
//    GlobalSettingsReadEntity globalSettings;

    public BillingProfileShortResponse toBoShortResponse() {
        return new BillingProfileShortResponse()
                .id(id)
                .subject(kyc != null ? kyc.subject() : kyb != null ? kyb.subject() : null)
                .type(type)
                .name(name)
                .verificationStatus(verificationStatus)
                .kyb(kyb == null ? null : kyb.toDto())
                .kyc(kyc == null ? null : kyc.toDto())
                ;
    }

    public Boolean invoiceMandateAccepted() {
        return invoiceMandateAcceptedAt != null;// &&
//               globalSettings.getInvoiceMandateLatestVersionDate() != null &&
//               invoiceMandateAcceptedAt.isAfter(globalSettings.getInvoiceMandateLatestVersionDate());
    }

    public Boolean individualLimitReached() {
        return type == BillingProfileType.INDIVIDUAL &&
               stats.getCurrentYearPaymentAmount().compareTo(BigDecimal.valueOf(5000)) >= 0;
    }
}
