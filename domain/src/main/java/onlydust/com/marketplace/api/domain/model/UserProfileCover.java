package onlydust.com.marketplace.api.domain.model;

public enum UserProfileCover {
    MAGENTA, CYAN, BLUE, YELLOW;

    public static UserProfileCover get(Long value) {
        final var values = UserProfileCover.values();
        return values[Long.valueOf(value % values.length).intValue()];
    }
}
