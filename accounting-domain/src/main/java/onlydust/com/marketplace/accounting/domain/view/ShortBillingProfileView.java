package onlydust.com.marketplace.accounting.domain.view;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;

@Builder
@Getter
public class ShortBillingProfileView {
    BillingProfile.Id id;
    BillingProfile.Type type;
    BillingProfile.User.Role role;
    VerificationStatus verificationStatus;
    String name;
    Boolean enabled;
    Boolean pendingInvitationResponse;
    @Getter(AccessLevel.NONE)
    @NonNull Boolean invoiceMandateAcceptanceOutdated;
    @Setter
    Integer rewardCount;
    Integer invoiceableRewardCount;
    Boolean missingPayoutInfo;
    Boolean missingVerification;
    Boolean individualLimitReached;

    public boolean isInvoiceMandateAccepted() {
        return !invoiceMandateAcceptanceOutdated;
    }

    public boolean isVerificationBlocked() {
        return verificationStatus.isBlocked();
    }

    public Integer requestableRewardCount() {
        if (invoiceableRewardCount == null || role == null)
            return null;

        return role == BillingProfile.User.Role.ADMIN ? invoiceableRewardCount : 0;
    }
}
