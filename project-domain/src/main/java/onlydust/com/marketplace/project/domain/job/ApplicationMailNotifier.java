package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;
import onlydust.com.marketplace.project.domain.port.output.ProjectApplicationStoragePort;

import java.util.List;

@AllArgsConstructor
public class ApplicationMailNotifier {

    private final OutboxPort projectMailOutboxPort;
    private final ProjectApplicationStoragePort projectApplicationStoragePort;

    public void notifyProjectApplicationsToReview() {
        final List<ProjectApplicationsToReviewByUser> applications = projectApplicationStoragePort.findProjectApplicationsToReview();
        applications.forEach(projectMailOutboxPort::push);
    }
}
