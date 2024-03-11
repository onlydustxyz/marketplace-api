package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.kernel.model.RewardStatus;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record BackofficeRewardView(
        @NonNull RewardId id,
        @NonNull RewardStatus status,
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
}
