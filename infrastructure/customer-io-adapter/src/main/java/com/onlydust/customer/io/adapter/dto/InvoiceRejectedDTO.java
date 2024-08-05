package com.onlydust.customer.io.adapter.dto;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.notification.InvoiceRejected;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.onlydust.customer.io.adapter.dto.MailDTO.getRewardNames;

@Builder
public record InvoiceRejectedDTO(@NonNull String rewardsDetails,
                                 @NonNull String reason,
                                 @NonNull Integer rewardsNumber,
                                 @NonNull String username,
                                 @NonNull String invoiceName,
                                 @NonNull String totalUsdAmount) {

    public static InvoiceRejectedDTO fromEvent(@NonNull String recipientLogin, @NonNull InvoiceRejected invoiceRejected) {
        return new InvoiceRejectedDTO(getRewardNames(invoiceRejected.rewards()),
                invoiceRejected.rejectionReason(),
                invoiceRejected.rewards().size(),
                recipientLogin,
                invoiceRejected.invoiceName(),
                invoiceRejected.rewards().stream()
                        .map(ShortReward::getDollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(3, RoundingMode.HALF_UP).toString());
    }

}
