package onlydust.com.marketplace.accounting.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

import java.time.ZonedDateTime;

@Builder
@Getter
public class ShortBillingProfileView {
    BillingProfile.Id id;
    BillingProfile.Type type;
    String name;
    Boolean enabled;
    Boolean pendingInvitationResponse;
    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateAcceptedAt;
    @Getter(AccessLevel.NONE)
    @Setter
    ZonedDateTime invoiceMandateLatestVersionDate;

    public boolean isInvoiceMandateAccepted() {
        if (type == BillingProfile.Type.INDIVIDUAL) return true;

        return invoiceMandateAcceptedAt != null &&
               invoiceMandateLatestVersionDate != null &&
               invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }
}
