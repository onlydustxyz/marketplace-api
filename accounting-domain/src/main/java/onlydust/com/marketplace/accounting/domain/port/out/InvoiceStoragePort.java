package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;

import java.util.List;

public interface InvoiceStoragePort {
    InvoicePreview preview(final @NonNull BillingProfile.Id billingProfileId, final @NonNull List<RewardId> rewardIds);

    void save(final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice invoice);

    void deleteDraftsOf(final @NonNull BillingProfile.Id billingProfileId);
}
