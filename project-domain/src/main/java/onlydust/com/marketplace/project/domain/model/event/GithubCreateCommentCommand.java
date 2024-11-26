package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;
import onlydust.com.marketplace.project.domain.model.Application;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("GithubCreateCommentCommand")
@Builder
public class GithubCreateCommentCommand extends Event {
    @NonNull
    Application.Id applicationId;
    @NonNull
    Long githubUserId;
    @NonNull
    Long issueId;
    @NonNull
    Long repoId;
    @NonNull
    Long issueNumber;
    @NonNull
    String body;
}
