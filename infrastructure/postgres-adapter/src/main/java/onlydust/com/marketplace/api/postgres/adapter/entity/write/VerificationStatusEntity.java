package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import onlydust.com.marketplace.project.domain.model.VerificationStatus;

public enum VerificationStatusEntity {
    NOT_STARTED,
    STARTED,
    UNDER_REVIEW,
    VERIFIED,
    REJECTED,
    CLOSED;

    public VerificationStatus toDomain() {
        return switch (this) {
            case CLOSED -> VerificationStatus.CLOSED;
            case STARTED -> VerificationStatus.STARTED;
            case REJECTED -> VerificationStatus.REJECTED;
            case VERIFIED -> VerificationStatus.VERIFIED;
            case NOT_STARTED -> VerificationStatus.NOT_STARTED;
            case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
        };
    }

    public static VerificationStatusEntity fromDomain(final VerificationStatus verificationStatus) {
        return switch (verificationStatus) {
            case CLOSED -> CLOSED;
            case NOT_STARTED -> NOT_STARTED;
            case REJECTED -> REJECTED;
            case STARTED -> STARTED;
            case UNDER_REVIEW -> UNDER_REVIEW;
            case VERIFIED -> VERIFIED;
        };
    }
}
