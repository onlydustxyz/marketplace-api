package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceDownload;
import onlydust.com.marketplace.accounting.domain.port.in.InvoiceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class InvoiceService implements InvoiceFacadePort {
    private final @NonNull InvoiceStoragePort invoiceStoragePort;
    private final @NonNull PdfStoragePort pdfStoragePort;

    @Override
    public Optional<Invoice> find(Invoice.@NonNull Id id) {
        return invoiceStoragePort.get(id);
    }

    @Override
    public Page<Invoice> findAll(final @NonNull List<Invoice.Id> ids, final @NonNull List<Invoice.Status> statuses, final @NonNull Integer pageIndex,
                                 final @NonNull Integer pageSize) {
        return invoiceStoragePort.findAll(ids, statuses, pageIndex, pageSize);
    }

    @Override
    public void update(Invoice.Id id, Invoice.Status status) throws OnlyDustException {
        final var invoice = invoiceStoragePort.get(id).orElseThrow(() -> OnlyDustException.notFound("Invoice %s not found".formatted(id)));

        if (status == null) return;

        if (status != Invoice.Status.APPROVED && status != Invoice.Status.REJECTED)
            throw forbidden("Cannot update invoice to status %s".formatted(status));

        invoiceStoragePort.update(invoice.status(status));
    }

    @Override
    public @NonNull InvoiceDownload download(Invoice.@NonNull Id invoiceId) {
        final var invoice = invoiceStoragePort.get(invoiceId)
                .orElseThrow(() -> notFound("Invoice %s not found".formatted(invoiceId)));

        final var pdf = pdfStoragePort.download(invoice.internalFileName());
        return new InvoiceDownload(pdf, invoice.externalFileName());
    }
}
