package onlydust.com.marketplace.api.webhook.dto;


import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;

import java.math.BigDecimal;
import java.util.List;

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
    List<String> rewardNames;


    public static InvoiceRejectedEventDTO fromEvent(final InvoiceRejected invoiceRejected) {
        return InvoiceRejectedEventDTO.builder()
                .invoiceName(invoiceRejected.invoiceName())
                .rejectionReason(invoiceRejected.rejectionReason())
                .recipientEmail(invoiceRejected.billingProfileAdminEmail())
                .recipientName(isNull(invoiceRejected.billingProfileAdminFirstName()) ? invoiceRejected.billingProfileAdminGithubLogin() :
                        invoiceRejected.billingProfileAdminFirstName())
                .rewardCount(invoiceRejected.rewardCount())
                .rewardNames(invoiceRejected.rewards().stream()
                        .map(r -> String.join(" - ", r.id().pretty(), r.projectName(), r.currencyCode(), r.amount().toString()))
                        .toList())
                .totalUsdAmount(invoiceRejected.rewards().stream()
                        .map(InvoiceRejected.ShortReward::dollarsEquivalent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();
    }

}
