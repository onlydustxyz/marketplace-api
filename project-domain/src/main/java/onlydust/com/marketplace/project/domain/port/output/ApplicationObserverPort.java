package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Hackathon;

import java.util.UUID;

public interface ApplicationObserverPort {
    void onApplicationCreated(Application application);

    void onApplicationAccepted(Application application, UUID projectLeadId);

    void onHackathonExternalApplicationDetected(GithubIssue issue, Long applicantId, Hackathon hackathon);

    void onApplicationRefused(Application application);
}
