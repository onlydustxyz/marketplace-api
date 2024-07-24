package onlydust.com.marketplace.user.domain.model;

import lombok.NonNull;

import java.util.Optional;

public class NotificationSettings {
    public record Project(@NonNull ProjectId projectId,
                          @NonNull Optional<Boolean> onGoodFirstIssueAdded) {
        public Project patchWith(Project other) {
            return new Project(
                    projectId,
                    other.onGoodFirstIssueAdded().isPresent() ? other.onGoodFirstIssueAdded() : onGoodFirstIssueAdded
            );
        }
    }
}
