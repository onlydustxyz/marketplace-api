package onlydust.com.marketplace.accounting.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;

import java.time.ZonedDateTime;

import static java.util.Objects.isNull;

@Builder(toBuilder = true)
@Getter
public class BillingProfileView {
    BillingProfile.Id id;
    BillingProfile.Type type;
    String name;
    Kyc kyc;
    Kyb kyb;
    BillingProfile.User me;
    PayoutInfo payoutInfo;

    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateAcceptedAt;
    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateLatestVersionDate;

    public VerificationStatus verificationStatus() {
        return isNull(kyc) ? kyb.getStatus() : kyc.getStatus();
    }

    public boolean isInvoiceMandateAccepted() {
        if (type == BillingProfile.Type.INDIVIDUAL) return true;

        return invoiceMandateAcceptedAt != null &&
               invoiceMandateLatestVersionDate != null &&
               invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }
}
