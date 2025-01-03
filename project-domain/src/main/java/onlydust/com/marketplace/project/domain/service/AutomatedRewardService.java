package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.CreateAndCloseIssueCommand;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.port.input.AutomatedRewardFacadePort;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectCurrencyStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectOrganizationRepoView;
import onlydust.com.marketplace.project.domain.view.ProjectOrganizationView;
import onlydust.com.marketplace.project.domain.view.RewardableItemView;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class AutomatedRewardService implements AutomatedRewardFacadePort {

    private final GithubSearchPort githubSearchPort;
    private final ProjectFacadePort projectFacadePort;
    private final RewardFacadePort rewardFacadePort;
    private final ProjectStoragePort projectStoragePort;
    private final ProjectCurrencyStoragePort projectCurrencyStoragePort;

    @Override
    public void createOtherWorkAndReward(String projectSlug, UserId projectLeadId, String repositoryName, String reason, String recipientLogin,
                                         String currencyCode, BigDecimal amount) {
        final var projectId = projectStoragePort.getProjectIdBySlug(projectSlug)
                .orElseThrow(() -> OnlyDustException.internalServerError("Project slug %s not found".formatted(projectSlug)));

        final ProjectOrganizationRepoView repository = projectStoragePort.getProjectOrganizations(projectId).stream().map(ProjectOrganizationView::getRepos)
                .flatMap(Collection::stream)
                .filter(projectOrganizationRepoView -> projectOrganizationRepoView.getName().equals(repositoryName))
                .findFirst()
                .orElseThrow(() -> OnlyDustException.internalServerError("Repository %s not found on project %s".formatted(repositoryName, projectSlug)));

        final UUID currencyId = projectCurrencyStoragePort.findCurrencyIdByCode(currencyCode)
                .orElseThrow(() -> OnlyDustException.internalServerError("Currency %s not found".formatted(currencyCode)));

        final GithubUserIdentity recipient = getRecipientGithubUserIdentity(recipientLogin);

        final RewardableItemView otherWork = projectFacadePort.createAndCloseIssueForProjectIdAndRepositoryId(
                CreateAndCloseIssueCommand.builder()
                        .projectId(projectId)
                        .projectLeadId(projectLeadId)
                        .githubRepoId(repository.getGithubRepoId())
                        .title("%s - Recipient github login : %s".formatted(reason, recipient.login()))
                        .description("Reward sent from OnlyDust admin")
                        .build());

        final RequestRewardCommand requestRewardCommand = RequestRewardCommand.builder()
                .amount(amount)
                .projectId(projectId)
                .currencyId(CurrencyView.Id.of(currencyId))
                .recipientId(recipient.githubUserId())
                .items(List.of(RequestRewardCommand.Item.builder()
                        .id(otherWork.getId())
                        .type(RequestRewardCommand.Item.Type.issue)
                        .number(otherWork.getNumber())
                        .repoId(repository.getGithubRepoId())
                        .build()))
                .build();

        rewardFacadePort.createReward(projectLeadId, requestRewardCommand);
    }

    private GithubUserIdentity getRecipientGithubUserIdentity(String recipientLogin) {
        final List<GithubUserIdentity> recipientResults = githubSearchPort.searchUsersByLogin(recipientLogin);
        if (recipientResults.isEmpty()) {
            throw OnlyDustException.internalServerError("Github user %s not found".formatted(recipientResults));
        }
        return recipientResults.stream()
                .filter(githubUserIdentity -> githubUserIdentity.login().equals(recipientLogin))
                .findFirst()
                .orElseThrow(() -> OnlyDustException.internalServerError("Github user %s not found in github search response".formatted(recipientLogin)));
    }

}
