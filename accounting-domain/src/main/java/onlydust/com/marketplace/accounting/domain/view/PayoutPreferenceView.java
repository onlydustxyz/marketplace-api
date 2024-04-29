package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;

@Builder(toBuilder = true)
public record PayoutPreferenceView(
        ShortBillingProfileView billingProfileView,
        @NonNull ProjectShortView shortProjectView) {
}
