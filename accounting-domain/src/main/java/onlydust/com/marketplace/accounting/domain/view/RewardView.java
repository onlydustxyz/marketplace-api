package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Network;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record RewardView(@NonNull UUID id,
                         @NonNull ZonedDateTime requestedAt,
                         ZonedDateTime processedAt,
                         @NonNull List<String> githubUrls,
                         @NonNull String projectName,
                         @NonNull String projectLogoUrl,
                         @NonNull List<ShortSponsorView> sponsors,
                         @NonNull MoneyView money,
                         @NonNull ShortBillingProfileAdminView billingProfileAdmin,
                         String transactionHash
) {

    @Deprecated
    public Network network() {
        return Network.fromCurrencyCode(money().currencyCode());
    }
}
