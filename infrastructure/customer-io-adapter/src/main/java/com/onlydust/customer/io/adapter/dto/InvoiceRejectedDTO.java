package com.onlydust.customer.io.adapter.dto;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.onlydust.customer.io.adapter.dto.MailDTO.getRewardNames;

@Builder
public record InvoiceRejectedDTO(@NonNull String rewardsDetails,
                                 @NonNull String reason,
                                 @NonNull String rewardsNumber,
                                 @NonNull String username,
                                 @NonNull String invoiceName,
                                 @NonNull String totalUsdAmount) {

    public static InvoiceRejectedDTO fromEvent(@NonNull InvoiceRejected invoiceRejected) {
        return new InvoiceRejectedDTO(getRewardNames(invoiceRejected.rewards()), invoiceRejected.rejectionReason(), invoiceRejected.rewardCount().toString(),
                invoiceRejected.billingProfileAdminGithubLogin(), invoiceRejected.invoiceName(),
                invoiceRejected.rewards().stream()
                        .map(ShortReward::getDollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(1, RoundingMode.HALF_UP).toString());
    }

}
