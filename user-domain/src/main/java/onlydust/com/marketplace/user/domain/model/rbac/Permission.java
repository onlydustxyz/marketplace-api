package onlydust.com.marketplace.user.domain.model.rbac;

public enum Permission {
    READ_ANY(false),                   // Eg. for a contributor, read contributor info
    READ(true),         // Eg. for a contributor, read contributor sensitive info
    READ_PRIVATE(true),         // Eg. for a contributor, read contributor sensitive info
    READ_SENSITIVE(true),         // Eg. for a contributor, read contributor sensitive info
    WRITE(true),                  // Eg. for a contributor, edit contributor info, KYC, KYB, etc.
    ;

    private final boolean requiresResourceId;

    public boolean requiresResourceId() {
        return requiresResourceId;
    }

    Permission(boolean requiresResourceId) {
        this.requiresResourceId = requiresResourceId;
    }
}
