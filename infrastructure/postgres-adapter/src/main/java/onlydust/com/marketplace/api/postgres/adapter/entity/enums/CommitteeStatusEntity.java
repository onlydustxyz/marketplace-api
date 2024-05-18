package onlydust.com.marketplace.api.postgres.adapter.entity.enums;

import onlydust.com.marketplace.project.domain.model.Committee;

public enum CommitteeStatusEntity {
    DRAFT, OPEN_TO_APPLICATIONS, OPEN_TO_VOTES, CLOSED;

    public static CommitteeStatusEntity fromDomain(final Committee.Status status) {
        return switch (status) {
            case DRAFT -> DRAFT;
            case CLOSED -> CLOSED;
            case OPEN_TO_VOTES -> OPEN_TO_VOTES;
            case OPEN_TO_APPLICATIONS -> OPEN_TO_APPLICATIONS;
        };
    }

    public Committee.Status toDomain() {
        return switch (this) {
            case DRAFT -> Committee.Status.DRAFT;
            case CLOSED -> Committee.Status.CLOSED;
            case OPEN_TO_VOTES -> Committee.Status.OPEN_TO_VOTES;
            case OPEN_TO_APPLICATIONS -> Committee.Status.OPEN_TO_APPLICATIONS;
        };
    }
}
