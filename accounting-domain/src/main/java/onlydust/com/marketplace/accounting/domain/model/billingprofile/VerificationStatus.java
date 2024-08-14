package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VerificationStatus {
    VERIFIED(-1),
    UNDER_REVIEW(0),
    STARTED(1),
    NOT_STARTED(2),
    REJECTED(3),
    CLOSED(4);

    final int priority;

}
