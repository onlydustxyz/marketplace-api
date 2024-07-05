package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("ProjectApplicationAccepted")
@Builder
public class ProjectApplicationAccepted extends Event {
    @NonNull
    UUID userId;
    @NonNull
    String email;
    @NonNull
    String userLogin;
    @NonNull
    Project project;
    @NonNull
    Issue issue;

    public record Project(@NonNull UUID id,
                          @NonNull String slug,
                          @NonNull String name) {
    }

    public record Issue(@NonNull Long id,
                        @NonNull String htmlUrl,
                        @NonNull String title,
                        @NonNull String repoName,
                        @NonNull String description) {
    }
}
