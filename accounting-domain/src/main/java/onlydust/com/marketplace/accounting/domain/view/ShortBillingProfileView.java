package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

@Builder
@Getter
public class ShortBillingProfileView {
    BillingProfile.Id id;
    BillingProfile.Type type;
    String name;
}
