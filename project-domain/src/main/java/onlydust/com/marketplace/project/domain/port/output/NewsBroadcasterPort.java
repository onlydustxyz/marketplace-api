package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.event.ProjectApplicationsToReviewByUser;

import java.util.List;

public interface NewsBroadcasterPort {

    void broadcastProjectApplicationsToReview(List<ProjectApplicationsToReviewByUser> applications);
}
