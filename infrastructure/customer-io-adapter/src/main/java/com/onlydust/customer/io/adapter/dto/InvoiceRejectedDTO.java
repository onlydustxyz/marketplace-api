package com.onlydust.customer.io.adapter.dto;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.onlydust.customer.io.adapter.dto.UrlMapper.getMarketplaceMyRewardsUrlFromEnvironment;

@Builder
public record InvoiceRejectedDTO(@NonNull String title,
                                 @NonNull String description,
                                 @NonNull Integer rewardsNumber,
                                 @NonNull String username,
                                 @NonNull List<RewardDTO> rewards,
                                 @NonNull ButtonDTO button) {
    private static final String DESCRIPTION = "We're very sorry but we had to reject your invoice named %s of <b>%s USD</b> for this reason: <b>%s</b>";

    public static InvoiceRejectedDTO fromEvent(@NonNull String recipientLogin, @NonNull InvoiceRejected invoiceRejected, @NonNull String environment) {
        return new InvoiceRejectedDTO("Invoice rejected",
                DESCRIPTION.formatted(invoiceRejected.invoiceName(), invoiceRejected.rewards().stream()
                        .map(ShortReward::dollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(3, RoundingMode.HALF_UP).toString(), invoiceRejected.rejectionReason()),
                invoiceRejected.rewards().size(),
                recipientLogin,
                invoiceRejected.rewards().stream()
                        .map(RewardDTO::from)
                        .toList(),
                new ButtonDTO("Upload another invoice", getMarketplaceMyRewardsUrlFromEnvironment(environment))
        );
    }

}
