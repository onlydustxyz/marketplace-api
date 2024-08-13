package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.NonNull;

public record IndividualKycIdentity(@NonNull String email, @NonNull String firstName, @NonNull String lastName) {
}
