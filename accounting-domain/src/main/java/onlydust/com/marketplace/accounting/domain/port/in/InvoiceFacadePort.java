package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceDownload;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;
import java.util.Optional;

public interface InvoiceFacadePort {

    Optional<Invoice> find(final @NonNull Invoice.Id id);

    Page<Invoice> findAll(final @NonNull List<Invoice.Id> ids, final @NonNull List<Invoice.Status> statuses, final @NonNull Integer pageIndex,
                          final @NonNull Integer pageSize);

    void update(@NonNull Invoice.Id id, @NonNull Invoice.Status status, String rejectionReason);

    @NonNull InvoiceDownload download(final @NonNull Invoice.Id invoiceId);
}
