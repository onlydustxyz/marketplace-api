package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record BillingProfileAdminView(@NonNull String githubLogin, @NonNull String email, String firstName) {
}
