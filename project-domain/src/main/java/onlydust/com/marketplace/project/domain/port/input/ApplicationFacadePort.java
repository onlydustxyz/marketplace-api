package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;

public interface ApplicationFacadePort {

    Application applyOnProject(@NonNull Long githubUserId,
                               @NonNull ProjectId projectId,
                               @NonNull GithubIssue.Id issueId,
                               @NonNull String githubComment);

    void updateApplication(@NonNull UserId userId,
                           @NonNull Application.Id applicationId,
                           Boolean isIgnored);

    void deleteApplication(Application.Id id, UserId userId, Long githubUserId, boolean deleteGithubComment);

    void acceptApplication(Application.Id id, UserId userId);
}
