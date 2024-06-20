package onlydust.com.marketplace.project.domain.model;

import lombok.NonNull;

import java.util.Map;

public record GithubAppAccessToken(@NonNull String token, @NonNull Map<String, String> permissions) {
    public boolean canWriteIssues() {
        return permissions.getOrDefault("issues", "none").equals("write");
    }
}
