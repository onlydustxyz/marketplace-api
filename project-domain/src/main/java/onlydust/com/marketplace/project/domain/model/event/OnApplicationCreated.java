package onlydust.com.marketplace.project.domain.model.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;

import java.time.ZonedDateTime;
import java.util.UUID;

@Value
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@EventType("OnApplicationCreated")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OnApplicationCreated extends Event {
    @NonNull
    Application.Id applicationId;
    @NonNull
    UUID projectId;
    @NonNull
    Long applicantId;
    @NonNull
    Application.Origin origin;
    @NonNull
    ZonedDateTime appliedAt;
    @NonNull
    GithubIssue.Id issueId;
    @NonNull
    GithubComment.Id commentId;

    public static OnApplicationCreated of(Application application) {
        return OnApplicationCreated.builder()
                .applicationId(application.id())
                .projectId(application.projectId())
                .applicantId(application.applicantId())
                .origin(application.origin())
                .appliedAt(application.appliedAt())
                .issueId(application.issueId())
                .commentId(application.commentId())
                .build();
    }
}
