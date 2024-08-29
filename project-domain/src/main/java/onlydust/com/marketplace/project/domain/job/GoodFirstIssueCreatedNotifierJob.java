package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.notification.GoodFirstIssueCreated;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationDetailedIssue;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationProject;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
public class GoodFirstIssueCreatedNotifierJob {

    private final GithubStoragePort githubStoragePort;
    private final ProjectStoragePort projectStoragePort;
    private final UserStoragePort userStoragePort;
    private final NotificationPort notificationPort;

    @Transactional
    public void run() {
        for (GithubIssue goodFirstIssueCreatedSince5Minutes : githubStoragePort.findGoodFirstIssuesCreatedSince5Minutes()) {
            final NotificationDetailedIssue notificationDetailedIssue = new NotificationDetailedIssue(goodFirstIssueCreatedSince5Minutes.id().value(),
                    goodFirstIssueCreatedSince5Minutes.htmlUrl(),
                    goodFirstIssueCreatedSince5Minutes.title(), goodFirstIssueCreatedSince5Minutes.repoName(),
                    goodFirstIssueCreatedSince5Minutes.description(),
                    goodFirstIssueCreatedSince5Minutes.authorLogin(),
                    goodFirstIssueCreatedSince5Minutes.authorAvatarUrl(),
                    goodFirstIssueCreatedSince5Minutes.labels()
            );
            for (UUID projectId : projectStoragePort.findProjectIdsByRepoId(goodFirstIssueCreatedSince5Minutes.repoId())) {
                final NotificationProject notificationProject = projectStoragePort.getById(projectId)
                        .map(project -> new NotificationProject(projectId, project.getSlug(), project.getName()))
                        .orElseThrow(() -> OnlyDustException.internalServerError("Project %s not found".formatted(projectId)));
                for (UserId userId : userStoragePort.findUserIdsRegisteredOnNotifyOnNewGoodFirstIssuesOnProject(projectId)) {
                    notificationPort.push(userId.value(), GoodFirstIssueCreated.builder()
                            .issue(notificationDetailedIssue)
                            .project(notificationProject)
                            .build());
                }
            }
        }
    }
}
