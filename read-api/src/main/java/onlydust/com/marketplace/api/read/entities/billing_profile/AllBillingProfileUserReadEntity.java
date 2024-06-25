package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.VerificationStatus;
import onlydust.com.marketplace.api.contract.model.BillingProfileCoworkerRole;
import onlydust.com.marketplace.api.contract.model.ShortBillingProfileResponse;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@IdClass(AllBillingProfileUserReadEntity.PrimaryKey.class)
@Table(name = "all_billing_profile_users", schema = "accounting")
public class AllBillingProfileUserReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID billingProfileId;

    @Id
    @EqualsAndHashCode.Include
    Long githubUserId;

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
                .name(billingProfile.name())
                .enabled(billingProfile.enabled())
                .invoiceMandateAccepted(!billingProfile.stats().mandateAcceptanceOutdated())
                .pendingInvitationResponse(!invitationAccepted)
                .invoiceableRewardCount(billingProfile.stats().invoiceableRewardCount())
                .requestableRewardCount(role == BillingProfileCoworkerRole.ADMIN ? billingProfile.stats().invoiceableRewardCount() : 0)
                .rewardCount(billingProfile.stats().rewardCount())
                .missingPayoutInfo(billingProfile.stats().missingPayoutInfo())
                .missingVerification(billingProfile.stats().missingVerification())
                .individualLimitReached(billingProfile.stats().individualLimitReached())
                .verificationBlocked(billingProfile.verificationStatus() == VerificationStatus.REJECTED || billingProfile.verificationStatus() == VerificationStatus.CLOSED)
                .role(role)
                .type(onlydust.com.marketplace.api.contract.model.BillingProfileType.valueOf(billingProfile.type().name()));
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID billingProfileId;
        Long githubUserId;
    }
}
