package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceDownload;
import onlydust.com.marketplace.accounting.domain.model.InvoiceView;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;
import java.util.Optional;

public interface InvoiceFacadePort {

    // TODO: move to read-api
    Optional<InvoiceView> find(final @NonNull Invoice.Id id);

    // TODO: move to read-api
    Page<Invoice> findAll(final @NonNull List<Invoice.Id> ids, final @NonNull List<Invoice.Status> statuses,
                          final @NonNull List<Currency.Id> currencyIds, final @NonNull List<BillingProfile.Type> billingProfileTypes,
                          final @NonNull List<BillingProfile.Id> billingProfileIds, final String search, final @NonNull Integer pageIndex,
                          final @NonNull Integer pageSize);

    void update(@NonNull Invoice.Id id, @NonNull Invoice.Status status, String rejectionReason);

    @NonNull
    InvoiceDownload download(final @NonNull Invoice.Id invoiceId);
}
