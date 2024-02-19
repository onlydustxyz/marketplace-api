package onlydust.com.marketplace.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VerificationStatus {
    VERIFIED(-1),
    NOT_STARTED(0),
    STARTED(1),
    UNDER_REVIEW(2),
    REJECTED(4),
    CLOSED(5);

    final int priority;
}
