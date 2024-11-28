package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import onlydust.com.marketplace.project.domain.model.GithubAppAccessToken;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.UpdateIssueCommand;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.project.domain.port.input.IssueFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.port.output.GithubApiPort;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
public class IssueService implements IssueFacadePort {

    private final PermissionPort permissionPort;
    private final ContributionStoragePort contributionStoragePort;
    private final GithubApiPort githubApiPort;
    private final GithubStoragePort githubStoragePort;
    private final GithubAppService githubAppService;
    private final ContributionObserverPort contributionObserverPort;

    @Override
    public void updateIssue(UserId projectLeadId, UpdateIssueCommand updateIssueCommand) {
        if (!permissionPort.canUserUpdateContribution(projectLeadId, updateIssueCommand.id()))
            throw forbidden(String.format("User %s must be project lead to update issue %s linked to its projects", projectLeadId,
                    updateIssueCommand.id()));

        if (nonNull(updateIssueCommand.archived())) {
            contributionStoragePort.archiveContribution(updateIssueCommand.id(), updateIssueCommand.archived());
            contributionObserverPort.onContributionsChanged(updateIssueCommand.id());
        }

        if (nonNull(updateIssueCommand.closed()) && updateIssueCommand.closed()) {
            final GithubIssue issue = githubStoragePort.findIssueByUUID(updateIssueCommand.id()).orElseThrow(() -> notFound("Issue not found"));
            final Long repoId = issue.repoId();
            final GithubAppAccessToken githubAppAccessToken = githubAppService.getInstallationTokenFor(repoId)
                    .orElseThrow(() -> internalServerError("Could not generate GitHub App token for repository %d".formatted(repoId)));
            if (githubAppAccessToken.canWriteIssues()) {
                githubApiPort.closeIssue(githubAppAccessToken.token(), repoId, issue.number());
            } else {
                throw forbidden("Github app installed on repo %s has not the permission to write on issue".formatted(issue.repoId()));
            }
        }
    }
}
