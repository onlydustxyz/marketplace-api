package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.UserId;

import java.time.ZonedDateTime;

public interface AccountingBillingProfileStorage {
    boolean isAdmin(UserId userId, BillingProfile.Id billingProfileId);

    void updateInvoiceMandateAcceptanceDate(BillingProfile.Id billingProfileId, ZonedDateTime now);
}
