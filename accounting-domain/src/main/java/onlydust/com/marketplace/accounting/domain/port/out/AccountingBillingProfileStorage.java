package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

public interface AccountingBillingProfileStorage {
    boolean isAdmin(UserId userId, BillingProfile.Id billingProfileId);
}
