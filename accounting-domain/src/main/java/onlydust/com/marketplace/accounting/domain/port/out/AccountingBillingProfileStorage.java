package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.UserId;

public interface AccountingBillingProfileStorage {
    boolean isAdmin(UserId userId, BillingProfile.Id billingProfileId);
}
