package onlydust.com.marketplace.api.postgres.adapter;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.*;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.user.domain.port.input.UserObserverPort;

import java.util.Set;

@AllArgsConstructor
public class PostgresBiProjectorAdapter implements AccountingObserverPort, ContributionObserverPort, ProjectObserverPort, UserObserverPort {
    private final BiRewardDataRepository biRewardDataRepository;
    private final BiContributionDataRepository biContributionDataRepository;
    private final BiPerContributorContributionDataRepository biPerContributorContributionDataRepository;
    private final BiProjectGrantsDataRepository biProjectGrantsDataRepository;
    private final BiProjectGlobalDataRepository biProjectGlobalDataRepository;
    private final BiProjectBudgetDataRepository biProjectBudgetDataRepository;
    private final BiProjectContributionsDataRepository biProjectContributionsDataRepository;
    private final BiContributorGlobalDataRepository biContributorGlobalDataRepository;
    private final BiContributionRewardDataRepository biContributionRewardDataRepository;
    private final UserProjectRecommendationsRepository userProjectRecommendationsRepository;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {

    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {

    }

    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        biRewardDataRepository.refresh(rewardId);
        biContributionRewardDataRepository.refresh(rewardId);
    }

    @Override
    public void onRewardCancelled(RewardId rewardId) {
        biRewardDataRepository.refresh(rewardId);
        biContributionRewardDataRepository.refresh(rewardId);
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {
        biRewardDataRepository.refresh(rewardId);
        biContributionRewardDataRepository.refresh(rewardId);
    }

    @Override
    public void onPayoutPreferenceChanged(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId) {

    }

    @Override
    public void onBillingProfileEnableChanged(BillingProfile.Id billingProfileId, Boolean enabled) {

    }

    @Override
    public void onBillingProfileDeleted(BillingProfile.Id billingProfileId) {

    }

    @Override
    public void onFundsAllocatedToProgram(SponsorId from, ProgramId to, PositiveAmount amount, Currency.Id currencyId) {

    }

    @Override
    public void onFundsRefundedByProgram(ProgramId from, SponsorId to, PositiveAmount amount, Currency.Id currencyId) {

    }

    @Override
    @Transactional
    public void onFundsGrantedToProject(ProgramId from, ProjectId to, PositiveAmount amount, Currency.Id currencyId) {
        biProjectGrantsDataRepository.refresh(from, to);
        biProjectBudgetDataRepository.refresh(to);
    }

    @Override
    @Transactional
    public void onFundsRefundedByProject(ProjectId from, ProgramId to, PositiveAmount amount, Currency.Id currencyId) {
        biProjectGrantsDataRepository.refresh(to, from);
        biProjectBudgetDataRepository.refresh(from);
    }

    @Override
    @Transactional
    public void onContributionsChanged(Long repoId) {
        biContributionDataRepository.refreshByRepo(repoId);
        biPerContributorContributionDataRepository.refreshByRepo(repoId);
        biProjectContributionsDataRepository.refresh(repoId);
        biContributionRewardDataRepository.refresh(repoId);
    }

    @Override
    @Transactional
    public void onLinkedReposChanged(ProjectId projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        linkedRepoIds.forEach(biContributionDataRepository::refreshByRepo);
        unlinkedRepoIds.forEach(biContributionDataRepository::refreshByRepo);
        linkedRepoIds.forEach(biPerContributorContributionDataRepository::refreshByRepo);
        unlinkedRepoIds.forEach(biPerContributorContributionDataRepository::refreshByRepo);
        biProjectGlobalDataRepository.refresh(projectId);
        biProjectContributionsDataRepository.refresh(projectId);
        linkedRepoIds.forEach(biContributionRewardDataRepository::refresh);
        unlinkedRepoIds.forEach(biContributionRewardDataRepository::refresh);
    }

    @Override
    public void onRewardSettingsChanged(ProjectId projectId) {

    }

    @Override
    @Transactional
    public void onProjectCreated(ProjectId projectId, UserId projectLeadId) {
        biProjectGlobalDataRepository.refresh(projectId);
        biProjectBudgetDataRepository.refresh(projectId);
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UserId userId) {

    }

    @Override
    @Transactional
    public void onUserSignedUp(AuthenticatedUser user) {
        biContributorGlobalDataRepository.refresh(user.githubUserId());
        userProjectRecommendationsRepository.refresh(user.githubUserId());
    }
}
