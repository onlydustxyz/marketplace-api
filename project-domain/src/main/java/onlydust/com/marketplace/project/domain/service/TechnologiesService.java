package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.TechnologiesPort;
import onlydust.com.marketplace.project.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.project.domain.port.output.TrackingIssuePort;

import java.util.List;

@AllArgsConstructor
public class TechnologiesService implements TechnologiesPort {
    private final TrackingIssuePort trackingIssuePort;
    private final TechnologyStoragePort technologyStoragePort;

    @Override
    public void suggest(String name, User requester) {
        trackingIssuePort.createIssueForTechTeam("New technology suggestion: " + name,
                "Suggested by: " + requester.getGithubLogin());
    }

    @Override
    public List<String> getAllUsedTechnologies() {
        return technologyStoragePort.getAllUsedTechnologies();
    }
}
