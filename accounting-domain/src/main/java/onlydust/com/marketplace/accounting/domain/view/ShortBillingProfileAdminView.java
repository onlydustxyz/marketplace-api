package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;

import java.util.List;

@Builder
public record ShortBillingProfileAdminView(@NonNull List<Admin> admins,
                                           @NonNull BillingProfile.Id billingProfileId,
                                           @NonNull BillingProfile.Type billingProfileType,
                                           String billingProfileName,
                                           VerificationStatus verificationStatus) {
    public record Admin(@NonNull String login,
                        @NonNull String avatarUrl,
                        @NonNull String email,
                        String firstName,
                        String lastName) {
    }
}
