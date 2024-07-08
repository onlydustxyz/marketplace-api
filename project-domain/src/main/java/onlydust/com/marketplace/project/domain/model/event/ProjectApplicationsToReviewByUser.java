package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("ProjectApplicationsToReviewByUser")
@Builder
public class ProjectApplicationsToReviewByUser extends Event {
    @NonNull
    UUID userId;
    @NonNull
    String email;
    @NonNull
    String userLogin;
    @NonNull
    List<Project> projects;

    public record Project(@NonNull UUID id,
                          @NonNull String slug,
                          @NonNull String name,
                          @NonNull List<Issue> issues) {
    }

    public record Issue(@NonNull Long id,
                        @NonNull String title,
                        @NonNull String repoName,
                        @NonNull Integer applicantCount) {
    }
}
