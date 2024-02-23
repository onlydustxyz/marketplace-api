package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
public record PayoutPreferenceView(
        ShortBillingProfileView shortBillingProfileView,
        @NonNull ShortProjectView shortProjectView) {
}
