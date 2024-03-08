package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.RewardId;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record RewardDetailsView(
        @NonNull RewardId id,
        @NonNull Status status,
        @NonNull ZonedDateTime requestedAt,
        ZonedDateTime processedAt,
        @NonNull List<String> githubUrls,
        @NonNull ShortProjectView project,
        @NonNull List<ShortSponsorView> sponsors,
        @NonNull MoneyView money,
        ShortBillingProfileAdminView billingProfileAdmin,
        ShortContributorView recipient,
        ShortInvoiceView invoice,
        @NonNull List<String> transactionReferences,
        @NonNull List<String> paidToAccountNumbers,
        ZonedDateTime paidNotificationSentAt
) {

    @Deprecated
    public Network network() {
        return Network.fromCurrencyCode(money().currencyCode());
    }

    public enum Status {
        PENDING_INVOICE,
        PENDING_SIGNUP,
        PENDING_CONTRIBUTOR,
        PENDING_VERIFICATION,
        MISSING_PAYOUT_INFO,
        PROCESSING,
        COMPLETE,
        LOCKED
    }
}
