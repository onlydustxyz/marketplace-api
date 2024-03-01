package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;

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
                         @NonNull ShortBillingProfileAdminView billingProfileAdmin
) {
}
