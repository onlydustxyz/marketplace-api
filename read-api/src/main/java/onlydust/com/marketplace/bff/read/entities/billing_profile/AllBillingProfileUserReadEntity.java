package onlydust.com.marketplace.bff.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.backoffice.api.contract.model.VerificationStatus;
import onlydust.com.marketplace.api.contract.model.BillingProfileCoworkerRole;
import onlydust.com.marketplace.api.contract.model.ShortBillingProfileResponse;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Value
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@IdClass(AllBillingProfileUserReadEntity.PrimaryKey.class)
public class AllBillingProfileUserReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID billingProfileId;

    @Id
    @EqualsAndHashCode.Include
    UUID userId;

    @ManyToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileReadEntity billingProfile;

    @Enumerated(EnumType.STRING)
    BillingProfileCoworkerRole role;

    boolean invitationAccepted;

    public ShortBillingProfileResponse toShortResponse() {
        return new ShortBillingProfileResponse()
                .id(billingProfileId)
                .name(billingProfile.getName())
                .enabled(billingProfile.getEnabled())
                .invoiceMandateAccepted(billingProfile.invoiceMandateAccepted())
                .pendingInvitationResponse(!invitationAccepted)
                .invoiceableRewardCount(billingProfile.getStats().getInvoiceableRewardCount())
                .requestableRewardCount(role == BillingProfileCoworkerRole.ADMIN ? billingProfile.getStats().getInvoiceableRewardCount() : 0)
                .rewardCount(billingProfile.getStats().getRewardCount())
                .missingPayoutInfo(billingProfile.getStats().getMissingPayoutInfo())
                .missingVerification(billingProfile.getStats().getMissingVerification())
                .individualLimitReached(billingProfile.individualLimitReached())
                .verificationBlocked(billingProfile.getVerificationStatus() == VerificationStatus.REJECTED || billingProfile.getVerificationStatus() == VerificationStatus.CLOSED)
                .role(role)
                .type(onlydust.com.marketplace.api.contract.model.BillingProfileType.valueOf(billingProfile.getType().name()));
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID billingProfileId;
        UUID userId;
    }
}
