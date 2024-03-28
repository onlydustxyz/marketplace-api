package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

@Builder(toBuilder = true)
public record PayoutPreferenceView(
        BillingProfileView billingProfileView,
        @NonNull ShortProjectView shortProjectView) {

    @Builder
    public record BillingProfileView(
            @NonNull BillingProfile.Id id,
            @NonNull String name,
            @NonNull BillingProfile.Type type) {
    }
}
