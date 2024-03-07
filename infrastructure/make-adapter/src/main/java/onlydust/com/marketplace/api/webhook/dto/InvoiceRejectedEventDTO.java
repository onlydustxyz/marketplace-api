package onlydust.com.marketplace.api.webhook.dto;


import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.util.Objects.isNull;

@Builder
@Data
public class InvoiceRejectedEventDTO {
    @NonNull
    String recipientEmail;
    @NonNull
    String recipientName;
    @NonNull
    Long rewardCount;
    @NonNull
    BigDecimal totalUsdAmount;
    String rejectionReason;
    @NonNull
    String invoiceName;
    @NonNull
    String rewardNames;


    public static InvoiceRejectedEventDTO fromEvent(final InvoiceRejected invoiceRejected) {
        return InvoiceRejectedEventDTO.builder()
                .invoiceName(invoiceRejected.invoiceName())
                .rejectionReason(invoiceRejected.rejectionReason())
                .recipientEmail(invoiceRejected.billingProfileAdminEmail())
                .recipientName(isNull(invoiceRejected.billingProfileAdminFirstName()) ? invoiceRejected.billingProfileAdminGithubLogin() :
                        invoiceRejected.billingProfileAdminFirstName())
                .rewardCount(invoiceRejected.rewardCount())
                .rewardNames(String.join("<br>", invoiceRejected.rewards().stream()
                        .map(r -> String.join(" - ", r.id().pretty(), r.projectName(), r.currencyCode(), r.amount().toString()))
                        .toList()))
                .totalUsdAmount(invoiceRejected.rewards().stream()
                        .map(InvoiceRejected.ShortReward::dollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(1, RoundingMode.HALF_UP))
                .build();
    }
}
