package onlydust.com.marketplace.accounting.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.model.RewardStatus;

public record RewardAssociations(@NonNull RewardId rewardId, @NonNull RewardStatus status,
                                 Invoice.Id invoiceId, Invoice.Status invoiceStatus,
                                 BillingProfile.Id billingProfileId) {
}
