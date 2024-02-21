package onlydust.com.marketplace.project.domain.model;

public enum UserProfileCover {
    MAGENTA, CYAN, BLUE, YELLOW;

    public static UserProfileCover get(Long index) {
        final var values = UserProfileCover.values();
        return values[Long.valueOf(index % values.length).intValue()];
    }
}
