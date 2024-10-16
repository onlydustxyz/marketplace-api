package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;

import java.time.ZonedDateTime;
import java.util.Optional;

@Value
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class OnApplicationCreatedTrackingEvent extends Event {
    @NonNull
    Application.Id applicationId;
    @NonNull
    ProjectId projectId;
    @NonNull
    Long applicantGithubId;
    UserId applicantUserId;
    @NonNull
    Application.Origin origin;
    @NonNull
    ZonedDateTime appliedAt;
    @NonNull
    GithubIssue.Id issueId;

    public static Event of(@NonNull OnApplicationCreated onApplicationCreated,
                           @NonNull Optional<UserId> applicantUserId) {
        return OnApplicationCreatedTrackingEvent.builder()
                .applicationId(onApplicationCreated.applicationId())
                .projectId(onApplicationCreated.projectId())
                .applicantGithubId(onApplicationCreated.applicantId())
                .applicantUserId(applicantUserId.orElse(null))
                .origin(onApplicationCreated.origin())
                .appliedAt(onApplicationCreated.appliedAt())
                .issueId(onApplicationCreated.issueId())
                .build();
    }
}
