package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.port.in.InvoiceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;

@AllArgsConstructor
public class InvoiceService implements InvoiceFacadePort {
    private final @NonNull InvoiceStoragePort invoiceStoragePort;

    @Override
    public Page<Invoice> findAllExceptDrafts(final @NonNull Integer pageIndex, final @NonNull Integer pageSize) {
        return invoiceStoragePort.findAllExceptDrafts(pageIndex, pageSize);
    }

    @Override
    public void update(Invoice.Id id, Invoice.Status status) throws OnlyDustException {
        final var invoice = invoiceStoragePort.get(id).orElseThrow(() -> OnlyDustException.notFound("Invoice %s not found".formatted(id)));

        if (status == null) return;

        if (status != Invoice.Status.APPROVED && status != Invoice.Status.REJECTED)
            throw forbidden("Cannot update invoice to status %s".formatted(status));

        invoiceStoragePort.save(invoice.status(status));
    }
}
