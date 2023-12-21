package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.TechnologiesPort;
import onlydust.com.marketplace.api.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.api.domain.port.output.TrackingIssuePort;

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
