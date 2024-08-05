package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationToReview;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class ApplicationMailNotifier implements ApplicationObserverPort {
    private final ProjectStoragePort projectStoragePort;
    private final GithubStoragePort githubStoragePort;
    private final UserStoragePort userStoragePort;
    private final NotificationPort notificationPort;

    @Override
    public void onApplicationCreated(Application application) {
        final var project = projectStoragePort.getById(application.projectId())
                .orElseThrow(() -> notFound("Project %s not found".formatted(application.projectId())));
        final var issue = githubStoragePort.findIssueById(application.issueId())
                .orElseThrow(() -> notFound("Issue %s not found".formatted(application.issueId())));

        projectStoragePort.getProjectLeadIds(application.projectId())
                .forEach(projectLeadId -> notificationPort.push(projectLeadId, ApplicationToReview.builder()
                        .project(new ApplicationToReview.Project(project.getId(), project.getSlug(), project.getName()))
                        .issue(new ApplicationToReview.Issue(issue.id().value(), issue.htmlUrl(), issue.title(), issue.repoName(), issue.description()))
                        .build()));
    }

    @Override
    public void onApplicationAccepted(Application application, UUID projectLeadId) {
        userStoragePort.getRegisteredUserByGithubId(application.applicantId()).ifPresent(applicant -> {
            final var project = projectStoragePort.getById(application.projectId())
                    .orElseThrow(() -> notFound("Project %s not found".formatted(application.projectId())));
            final var issue = githubStoragePort.findIssueById(application.issueId())
                    .orElseThrow(() -> notFound("Issue %s not found".formatted(application.issueId())));
            notificationPort.push(applicant.id(), ApplicationAccepted.builder()
                    .project(new ApplicationAccepted.Project(project.getId(), project.getSlug(), project.getName()))
                    .issue(new ApplicationAccepted.Issue(issue.id().value(), issue.htmlUrl(), issue.title(), issue.repoName(), issue.description()))
                    .build());
        });
    }

    @Override
    public void onHackathonExternalApplicationDetected(GithubIssue issue, Long applicantId, Hackathon hackathon) {
    }
}
