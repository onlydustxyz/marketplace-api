package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.User;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Value
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class OnApplicationCreatedTrackingEvent extends Event {
    @NonNull
    Application.Id applicationId;
    @NonNull
    UUID projectId;
    @NonNull
    Long applicantGithubId;
    UUID applicantUserId;
    @NonNull
    Application.Origin origin;
    @NonNull
    ZonedDateTime appliedAt;
    @NonNull
    GithubIssue.Id issueId;

    public static Event of(@NonNull OnApplicationCreated onApplicationCreated,
                           @NonNull Optional<User> applicant) {
        return OnApplicationCreatedTrackingEvent.builder()
                .applicationId(onApplicationCreated.applicationId())
                .projectId(onApplicationCreated.projectId())
                .applicantGithubId(onApplicationCreated.applicantId())
                .applicantUserId(applicant.map(User::getId).orElse(null))
                .origin(onApplicationCreated.origin())
                .appliedAt(onApplicationCreated.appliedAt())
                .issueId(onApplicationCreated.issueId())
                .build();
    }
}
