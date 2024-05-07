package onlydust.com.marketplace.accounting.domain.model;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.RewardShortView;
import onlydust.com.marketplace.accounting.domain.view.UserView;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record InvoiceView(
        @NonNull Invoice.Id id,
        @NonNull Invoice.BillingProfileSnapshot billingProfileSnapshot,
        @NonNull UserView createdBy,
        @NonNull ZonedDateTime createdAt,
        @NonNull Money totalAfterTax,
        @NonNull ZonedDateTime dueAt,
        @NonNull Invoice.Number number,
        @NonNull Invoice.Status status,
        @NonNull List<RewardShortView> rewards,
        URL url,
        String originalFileName,
        String rejectionReason
) {
}
