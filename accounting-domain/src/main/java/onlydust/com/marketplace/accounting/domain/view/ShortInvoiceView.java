package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;

@Builder
public record ShortInvoiceView(
        @NonNull Invoice.Id id,
        @NonNull Invoice.Number number,
        @NonNull Invoice.Status status,
        @NonNull UserView createdBy
) {
}
