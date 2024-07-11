package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;
import onlydust.com.marketplace.project.domain.port.output.*;

import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class ApplicationMailNotifier implements ApplicationObserverPort {

    private final OutboxPort projectMailOutboxPort;
    private final ProjectApplicationStoragePort projectApplicationStoragePort;
    private final ProjectStoragePort projectStoragePort;
    private final GithubStoragePort githubStoragePort;
    private final UserStoragePort userStoragePort;
    private final NewsBroadcasterPort newsBroadcasterPort;

    public void notifyProjectApplicationsToReview() {
        newsBroadcasterPort.broadcastProjectApplicationsToReview(projectApplicationStoragePort.findProjectApplicationsToReview());
    }

    @Override
    public void onApplicationCreated(Application application) {
    }

    @Override
    public void onApplicationAccepted(Application application) {
        userStoragePort.getRegisteredUserByGithubId(application.applicantId()).ifPresent(applicant -> {
            final var project = projectStoragePort.getById(application.projectId())
                    .orElseThrow(() -> notFound("Project %s not found".formatted(application.projectId())));
            final var issue = githubStoragePort.findIssueById(application.issueId())
                    .orElseThrow(() -> notFound("Issue %s not found".formatted(application.issueId())));
            projectMailOutboxPort.push(new ProjectApplicationAccepted(
                    applicant.getId(),
                    applicant.getGithubEmail(),
                    applicant.getGithubLogin(),
                    new ProjectApplicationAccepted.Project(project.getId(), project.getSlug(), project.getName()),
                    new ProjectApplicationAccepted.Issue(issue.id().value(), issue.htmlUrl(), issue.title(), issue.repoName(), issue.description())
            ));
        });
    }
}
