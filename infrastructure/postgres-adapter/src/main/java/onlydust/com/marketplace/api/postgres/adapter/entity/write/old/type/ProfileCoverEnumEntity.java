package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import onlydust.com.marketplace.api.domain.model.UserProfileCover;

public enum ProfileCoverEnumEntity {
    cyan,magenta,yellow,blue;

    public UserProfileCover toDomain() {
        return switch (this) {
            case cyan -> UserProfileCover.CYAN;
            case magenta -> UserProfileCover.MAGENTA;
            case yellow -> UserProfileCover.YELLOW;
            case blue -> UserProfileCover.BLUE;
        };
    }
}
