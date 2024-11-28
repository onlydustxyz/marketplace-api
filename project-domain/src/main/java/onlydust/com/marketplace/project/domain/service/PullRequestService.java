package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import onlydust.com.marketplace.project.domain.model.UpdatePullRequestCommand;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.project.domain.port.input.PullRequestFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;

@AllArgsConstructor
public class PullRequestService implements PullRequestFacadePort {
    private final PermissionPort permissionPort;
    private final ContributionStoragePort contributionStoragePort;
    private final ContributionObserverPort contributionObserverPort;

    @Override
    public void updatePullRequest(UserId projectLeadId, UpdatePullRequestCommand updatePullRequestCommand) {
        if (!permissionPort.canUserUpdateContribution(projectLeadId, updatePullRequestCommand.id())) {
            throw forbidden(String.format("User %s must be project lead to update pull request %s linked to its projects", projectLeadId,
                    updatePullRequestCommand.id().value()));
        }
        if (nonNull(updatePullRequestCommand.archived())) {
            contributionStoragePort.archiveContribution(updatePullRequestCommand.id(), updatePullRequestCommand.archived());
            contributionObserverPort.onContributionsChanged(updatePullRequestCommand.id());
        }
    }
}
