package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

@Builder
public class BillingProfileView {
    BillingProfile.Id id;
    BillingProfile.Type type;
}
