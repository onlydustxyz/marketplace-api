package onlydust.com.marketplace.project.domain.model;

import lombok.NonNull;

import java.util.UUID;

public record ProjectCategory(@NonNull UUID id, @NonNull String name, @NonNull Status status, String iconUrl) {

    public static ProjectCategory suggest(@NonNull String name) {
        return new ProjectCategory(UUID.randomUUID(), name, Status.SUGGESTED, null);
    }

    public enum Status {
        SUGGESTED, APPROVED;
    }
}
