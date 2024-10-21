package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import onlydust.com.marketplace.project.domain.model.UpdateIssueCommand;
import onlydust.com.marketplace.project.domain.port.input.IssueFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.unauthorized;

@AllArgsConstructor
public class IssueService implements IssueFacadePort {

    private final PermissionPort permissionPort;
    private final ContributionStoragePort contributionStoragePort;

    @Override
    public void updateIssue(UserId projectLeadId, UpdateIssueCommand updateIssueCommand) {
        if (!permissionPort.canUserUpdateContribution(projectLeadId, updateIssueCommand.id()))
            throw unauthorized(String.format("User %s must be project lead to update issue %s linked to its projects", projectLeadId,
                    updateIssueCommand.id()));

        if (nonNull(updateIssueCommand.archived()))
            contributionStoragePort.archiveContribution(updateIssueCommand.id(), updateIssueCommand.archived());
    }
}
