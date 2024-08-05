package onlydust.com.marketplace.project.domain.model.notification.dto;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Project;

import java.util.UUID;

public record NotificationProject(@NonNull UUID id,
                                  @NonNull String slug,
                                  @NonNull String name) {
    public static NotificationProject of(Project project) {
        return new NotificationProject(project.getId(), project.getSlug(), project.getName());
    }
}
