package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;

import java.util.UUID;

public interface ApplicationFacadePort {

    Application applyOnProject(@NonNull Long githubUserId,
                               @NonNull UUID projectId,
                               @NonNull GithubIssue.Id issueId,
                               @NonNull String motivation,
                               String problemSolvingApproach);

    Application updateApplication(@NonNull Application.Id applicationId,
                                  @NonNull Long githubUserId,
                                  @NonNull String motivation,
                                  String problemSolvingApproach);

    void deleteApplication(Application.Id id, UUID userId, Long githubUserId);

    void acceptApplication(Application.Id id, UUID userId);
}
