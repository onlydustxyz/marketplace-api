package onlydust.com.marketplace.project.domain.job.githubcommands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.model.event.GithubCreateCommentCommand;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectApplicationStoragePort;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
public class GithubCreateCommentCommandConsumerCleaner {
    private final ProjectApplicationStoragePort projectApplicationStoragePort;
    private final ApplicationObserverPort applicationObserver;

    @Transactional
    public void cleanupOnFailure(GithubCreateCommentCommand c) {
        projectApplicationStoragePort.findApplication(c.getApplicationId()).ifPresent(application -> {
            projectApplicationStoragePort.deleteApplications(c.getApplicationId());
            applicationObserver.onApplicationDeleted(application);
        });
    }
}
