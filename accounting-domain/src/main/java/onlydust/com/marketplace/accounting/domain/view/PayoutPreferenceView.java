package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder(toBuilder = true)
public record PayoutPreferenceView(
        ShortBillingProfileView shortBillingProfileView,
        @NonNull ShortProjectView shortProjectView) {
}
