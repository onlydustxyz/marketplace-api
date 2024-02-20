package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.kernel.pagination.Page;

public interface InvoiceFacadePort {
    Page<Invoice> findAllExceptDrafts(final @NonNull Integer pageIndex, final @NonNull Integer pageSize);
}
