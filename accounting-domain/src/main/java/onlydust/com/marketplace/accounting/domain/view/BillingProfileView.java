package onlydust.com.marketplace.accounting.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;

import java.time.ZonedDateTime;

@Value
@Builder(toBuilder = true)
@Getter
public class BillingProfileView {
    BillingProfile.Id id;
    BillingProfile.Type type;
    String name;
    Kyc kyc;
    Kyb kyb;
    BillingProfileUserRightsView me;
    VerificationStatus verificationStatus;
    PayoutInfo payoutInfo;
    Boolean enabled;

    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateAcceptedAt;
    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateLatestVersionDate;
    Integer rewardCount;
    Integer invoiceableRewardCount;
    Boolean missingPayoutInfo;
    Boolean missingVerification;

    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public boolean isInvoiceMandateAccepted() {
        if (type == BillingProfile.Type.INDIVIDUAL) return true;

        return invoiceMandateAcceptedAt != null &&
               invoiceMandateLatestVersionDate != null &&
               invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }

    public boolean isSwitchableToSelfEmployed() {
        return this.type == BillingProfile.Type.COMPANY && !this.me.hasMoreThanOneCoworkers();
    }

    public boolean isVerificationBlocked() {
        return verificationStatus.isBlocked();
    }
}
