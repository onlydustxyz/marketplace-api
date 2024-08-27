package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.RewardStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record RewardDetailsView(
        @NonNull RewardId id,
        Payment.Id paymentId,
        @NonNull RewardStatus status,
        @NonNull ZonedDateTime requestedAt,
        ZonedDateTime processedAt,
        @NonNull List<String> githubUrls,
        @NonNull ProjectShortView project,
        @NonNull List<SponsorView> sponsors,
        @NonNull MoneyView money,
        BillingProfile billingProfile,
        ShortContributorView recipient,
        InvoiceView invoice,
        List<Receipt> receipts,
        ZonedDateTime paidNotificationSentAt,
        Map<Network, PositiveAmount> pendingPayments,
        @NonNull ShortContributorView requester
) {

    @Deprecated
    public Network network() {
        return money().currency().legacyNetwork();
    }

    public List<String> transactionReferences() {
        return receipts.stream().map(Receipt::reference).toList();
    }

    public List<String> paidToAccountNumbers() {
        return receipts.stream().map(Receipt::thirdPartyAccountNumber).toList();
    }
}
