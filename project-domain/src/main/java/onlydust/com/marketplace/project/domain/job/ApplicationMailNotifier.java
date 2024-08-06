package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationToReview;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationIssue;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationProject;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationUser;
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
        final NotificationUser notificationUser = userStoragePort.getRegisteredUserByGithubId(application.applicantId())
                .map(user -> new NotificationUser(user.id(), user.githubUserId(), user.login()))
                .orElseGet(() -> userStoragePort.getIndexedUserByGithubId(application.applicantId())
                        .map(githubUserIdentity -> new NotificationUser(null, githubUserIdentity.githubUserId(), githubUserIdentity.login()))
                        .orElseThrow(() ->
                                OnlyDustException.internalServerError("Cannot send application created to project leads due to unknown github user %s"
                                        .formatted(application.applicantId()))));

        projectStoragePort.getProjectLeadIds(application.projectId())
                .forEach(projectLeadId -> notificationPort.push(projectLeadId, ApplicationToReview.builder()
                        .project(NotificationProject.of(project))
                        .issue(NotificationIssue.of(issue))
                        .user(notificationUser)
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
                    .project(NotificationProject.of(project))
                    .issue(NotificationIssue.of(issue))
                    .build());
        });
    }

    @Override
    public void onHackathonExternalApplicationDetected(GithubIssue issue, Long applicantId, Hackathon hackathon) {
    }
}
