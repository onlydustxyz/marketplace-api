package onlydust.com.marketplace.accounting.domain.view;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;

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

    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public boolean isInvoiceMandateAccepted() {
        if (type == BillingProfile.Type.INDIVIDUAL) return true;

        return invoiceMandateAcceptedAt != null &&
               invoiceMandateLatestVersionDate != null &&
               invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }
}
