package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.port.in.InvoiceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.kernel.pagination.Page;

@AllArgsConstructor
public class InvoiceService implements InvoiceFacadePort {
    private final @NonNull InvoiceStoragePort invoiceStoragePort;

    @Override
    public Page<Invoice> findAllExceptDrafts(final @NonNull Integer pageIndex, final @NonNull Integer pageSize) {
        return invoiceStoragePort.findAllExceptDrafts(pageIndex, pageSize);
    }
}
