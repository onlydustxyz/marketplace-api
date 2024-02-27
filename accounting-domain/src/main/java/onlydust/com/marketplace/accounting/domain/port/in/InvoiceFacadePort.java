package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceDownload;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;

public interface InvoiceFacadePort {
    Page<Invoice> findAll(final @NonNull List<Invoice.Id> ids, final @NonNull List<Invoice.Status> statuses, final @NonNull Integer pageIndex,
                          final @NonNull Integer pageSize);

    void update(Invoice.Id id, Invoice.Status status);

    @NonNull InvoiceDownload download(final @NonNull Invoice.Id invoiceId);
}
