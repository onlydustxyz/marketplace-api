package com.onlydust.customer.io.adapter.dto;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;

@Builder
public record InvoiceRejectedDTO(@NonNull String rewardsDetails,
                                 @NonNull String reason,
                                 @NonNull String rewardsNumber,
                                 @NonNull String username,
                                 @NonNull String invoiceName,
                                 @NonNull String totalUsdAmount) {

    public static InvoiceRejectedDTO fromEvent(@NonNull InvoiceRejected invoiceRejected) {
        return new InvoiceRejectedDTO();
    }
}
