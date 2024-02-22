package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import onlydust.com.marketplace.project.domain.model.OldVerificationStatus;

public enum OldVerificationStatusEntity {
    NOT_STARTED,
    STARTED,
    UNDER_REVIEW,
    VERIFIED,
    REJECTED,
    CLOSED;

    public OldVerificationStatus toDomain() {
        return switch (this) {
            case CLOSED -> OldVerificationStatus.CLOSED;
            case STARTED -> OldVerificationStatus.STARTED;
            case REJECTED -> OldVerificationStatus.REJECTED;
            case VERIFIED -> OldVerificationStatus.VERIFIED;
            case NOT_STARTED -> OldVerificationStatus.NOT_STARTED;
            case UNDER_REVIEW -> OldVerificationStatus.UNDER_REVIEW;
        };
    }

    public static OldVerificationStatusEntity fromDomain(final OldVerificationStatus oldVerificationStatus) {
        return switch (oldVerificationStatus) {
            case CLOSED -> CLOSED;
            case NOT_STARTED -> NOT_STARTED;
            case REJECTED -> REJECTED;
            case STARTED -> STARTED;
            case UNDER_REVIEW -> UNDER_REVIEW;
            case VERIFIED -> VERIFIED;
        };
    }
}
