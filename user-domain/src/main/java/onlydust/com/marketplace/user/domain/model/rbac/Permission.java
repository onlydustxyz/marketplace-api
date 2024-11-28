package onlydust.com.marketplace.user.domain.model.rbac;

public enum Permission {
    READ(false),                   // Eg. for a contributor, read contributor info
    READ_SENSITIVE(true),         // Eg. for a contributor, read contributor sensitive info
    WRITE(true),                  // Eg. for a contributor, edit contributor info, KYC, KYB, etc.
    READ_FINANCIALS(true),        // Eg. for a contributor, see rewards, etc.
    WRITE_FINANCIALS(true),       // Eg. for a contributor, edit billing profiles, etc.
    ;

    private final boolean requiresResourceId;

    public boolean requiresResourceId() {
        return requiresResourceId;
    }

    Permission(boolean requiresResourceId) {
        this.requiresResourceId = requiresResourceId;
    }
}
