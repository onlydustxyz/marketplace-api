package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.io.InputStream;
import java.util.List;

public interface BillingProfileFacadePort {
    Invoice previewInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull List<RewardId> rewardIds);

    Page<Invoice> invoicesOf(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber,
                             final @NonNull Integer pageSize);

    void uploadInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                       final @NonNull InputStream inputStream);
}
