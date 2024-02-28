package onlydust.com.marketplace.accounting.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;

import java.time.ZonedDateTime;

@Builder(toBuilder = true)
@Getter
public class BillingProfileView {
    BillingProfile.Id id;
    BillingProfile.Type type;
    String name;
    Kyc kyc;
    Kyb kyb;
    BillingProfile.User me;
    VerificationStatus verificationStatus;

    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateAcceptedAt;
    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateLatestVersionDate;

    public boolean isInvoiceMandateAccepted() {
        if (type == BillingProfile.Type.INDIVIDUAL) return true;

        return invoiceMandateAcceptedAt != null &&
               invoiceMandateLatestVersionDate != null &&
               invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }
}
