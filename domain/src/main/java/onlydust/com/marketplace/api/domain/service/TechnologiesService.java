package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.TechnologiesPort;
import onlydust.com.marketplace.api.domain.port.output.TrackingIssuePort;

@AllArgsConstructor
public class TechnologiesService implements TechnologiesPort {
    private final TrackingIssuePort trackingIssuePort;

    @Override
    public void suggest(String name, User requester) {
        trackingIssuePort.createIssueForTechTeam("New technology suggestion: " + name, "Suggested by: " + requester.getLogin());
    }
}
