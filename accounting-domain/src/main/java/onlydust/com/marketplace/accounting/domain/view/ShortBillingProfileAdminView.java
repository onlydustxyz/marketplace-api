package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

@Builder
public record ShortBillingProfileAdminView(@NonNull String adminGithubLogin,
                                           @NonNull String adminGithubAvatarUrl,
                                           @NonNull String adminEmail,
                                           String adminName,
                                           @NonNull BillingProfile.Id billingProfileId,
                                           @NonNull BillingProfile.Type billingProfileType,
                                           String billingProfileName) {
}
