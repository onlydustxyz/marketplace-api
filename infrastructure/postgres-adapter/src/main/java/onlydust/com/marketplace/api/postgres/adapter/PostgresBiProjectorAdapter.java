package onlydust.com.marketplace.api.postgres.adapter;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.*;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;
import onlydust.com.marketplace.user.domain.port.input.UserObserverPort;

import java.util.Set;

@AllArgsConstructor
public class PostgresBiProjectorAdapter implements AccountingObserverPort, ContributionObserverPort, ProjectObserverPort, UserObserverPort,
        ApplicationObserverPort {
    private final EntityManager entityManager;
    private final BiRewardDataRepository biRewardDataRepository;
    private final BiContributionDataRepository biContributionDataRepository;
    private final BiContributionContributorsDataRepository biContributionContributorsDataRepository;
    private final BiPerContributorContributionDataRepository biPerContributorContributionDataRepository;
    private final BiProjectGrantsDataRepository biProjectGrantsDataRepository;
    private final BiProjectGlobalDataRepository biProjectGlobalDataRepository;
    private final BiProjectBudgetDataRepository biProjectBudgetDataRepository;
    private final BiProjectContributionsDataRepository biProjectContributionsDataRepository;
    private final BiContributorGlobalDataRepository biContributorGlobalDataRepository;
    private final BiContributorApplicationDataRepository biContributorApplicationDataRepository;
    private final BiContributorRewardDataRepository biContributorRewardDataRepository;
    private final BiContributionRewardDataRepository biContributionRewardDataRepository;
    private final UserProjectRecommendationsRepository userProjectRecommendationsRepository;
    private final BiApplicationDataRepository biApplicationDataRepository;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {
    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {
    }

    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        entityManager.flush();
        biRewardDataRepository.refresh(rewardId);
        biContributionRewardDataRepository.refreshByReward(rewardId);
        biContributorRewardDataRepository.refreshByReward(rewardId);
    }

    @Override
    public void onRewardCancelledBefore(RewardId rewardId) {
    }

    @Override
    public void onRewardCancelledAfter(RewardId rewardId) {
        entityManager.flush();
        biRewardDataRepository.refresh(rewardId);
        biContributionRewardDataRepository.refreshByReward(rewardId);
        biContributorRewardDataRepository.refreshByReward(rewardId);
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {
        entityManager.flush();
        biRewardDataRepository.refresh(rewardId);
        biContributionRewardDataRepository.refreshByReward(rewardId);
        biContributorRewardDataRepository.refreshByReward(rewardId);
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
        entityManager.flush();
        biProjectGrantsDataRepository.refresh(from, to);
        biProjectBudgetDataRepository.refreshByProject(to);
    }

    @Override
    @Transactional
    public void onFundsRefundedByProject(ProjectId from, ProgramId to, PositiveAmount amount, Currency.Id currencyId) {
        entityManager.flush();
        biProjectGrantsDataRepository.refresh(to, from);
        biProjectBudgetDataRepository.refreshByProject(from);
    }

    @Override
    @Transactional
    public void onContributionsChanged(ContributionUUID contributionUUID) {
        entityManager.flush(); // For archived status change
        biContributionDataRepository.refreshByUUID(contributionUUID);
        biContributionContributorsDataRepository.refreshByUUID(contributionUUID);
        biPerContributorContributionDataRepository.refreshByUUID(contributionUUID);
        biContributionRewardDataRepository.refreshByUUID(contributionUUID);
        biApplicationDataRepository.refreshByContributionUUID(contributionUUID);
    }

    @Override
    @Transactional
    public void onContributionsChanged(Long repoId, ContributionUUID contributionUUID) {
        // No need to flush here, as the updates have been done on indexer side
        biContributionDataRepository.refreshByUUID(contributionUUID);
        biContributionContributorsDataRepository.refreshByUUID(contributionUUID);
        biPerContributorContributionDataRepository.refreshByUUID(contributionUUID);
        biProjectContributionsDataRepository.refreshByRepo(repoId);
        biContributionRewardDataRepository.refreshByUUID(contributionUUID);
        biApplicationDataRepository.refreshByContributionUUID(contributionUUID);
    }

    @Override
    @Transactional
    public void onLinkedReposChanged(ProjectId projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        entityManager.flush();
        linkedRepoIds.forEach(biContributionDataRepository::refreshByRepo);
        unlinkedRepoIds.forEach(biContributionDataRepository::refreshByRepo);
        linkedRepoIds.forEach(biContributionContributorsDataRepository::refreshByRepo);
        unlinkedRepoIds.forEach(biContributionContributorsDataRepository::refreshByRepo);
        linkedRepoIds.forEach(biPerContributorContributionDataRepository::refreshByRepo);
        unlinkedRepoIds.forEach(biPerContributorContributionDataRepository::refreshByRepo);
        biProjectGlobalDataRepository.refreshByProject(projectId);
        biProjectContributionsDataRepository.refreshByProject(projectId);
        linkedRepoIds.forEach(biContributionRewardDataRepository::refreshByRepo);
        unlinkedRepoIds.forEach(biContributionRewardDataRepository::refreshByRepo);
        linkedRepoIds.forEach(biApplicationDataRepository::refreshByRepo);
        unlinkedRepoIds.forEach(biApplicationDataRepository::refreshByRepo);
    }

    @Override
    public void onRewardSettingsChanged(ProjectId projectId) {
    }

    @Override
    @Transactional
    public void onProjectCreated(ProjectId projectId, UserId projectLeadId) {
        entityManager.flush();
        biProjectGlobalDataRepository.refreshByProject(projectId);
        biProjectBudgetDataRepository.refreshByProject(projectId);
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UserId userId) {
    }

    @Override
    @Transactional
    public void onLabelsModified(@NonNull ProjectId projectId, Set<Long> githubUserIds) {
    }

    @Override
    @Transactional
    public void onUserSignedUp(AuthenticatedUser user) {
        entityManager.flush();
        biContributorGlobalDataRepository.refresh(user.githubUserId());
        biContributorApplicationDataRepository.refresh(user.githubUserId());
        biContributorRewardDataRepository.refresh(user.githubUserId());
        userProjectRecommendationsRepository.refresh(user.githubUserId());
    }

    @Override
    @Transactional
    public void onApplicationCreated(Application application) {
        entityManager.flush();
        final var contributionUUID = ContributionUUID.of(application.issueId().value());
        biContributionContributorsDataRepository.refreshByUUID(contributionUUID);
        biPerContributorContributionDataRepository.refreshByUUID(contributionUUID);
        biContributorApplicationDataRepository.refresh(application.applicantId());
        biApplicationDataRepository.refresh(application.id());
    }

    @Override
    @Transactional
    public void onApplicationAccepted(Application application, UserId projectLeadId) {
        entityManager.flush();
        final var contributionUUID = ContributionUUID.of(application.issueId().value());
        biContributionContributorsDataRepository.refreshByUUID(contributionUUID);
        biPerContributorContributionDataRepository.refreshByUUID(contributionUUID);
        biContributorApplicationDataRepository.refresh(application.applicantId());
        biApplicationDataRepository.refresh(application.id());
    }

    @Override
    @Transactional
    public void onApplicationRefused(Application application) {
        entityManager.flush();
        biContributorApplicationDataRepository.refresh(application.applicantId());
        biApplicationDataRepository.refresh(application.id());
    }

    @Override
    @Transactional
    public void onApplicationDeleted(Application application) {
        entityManager.flush();
        final var contributionUUID = ContributionUUID.of(application.issueId().value());
        biContributionContributorsDataRepository.refreshByUUID(contributionUUID);
        biPerContributorContributionDataRepository.refreshByUUID(contributionUUID);
        biContributorApplicationDataRepository.refresh(application.applicantId());
        biApplicationDataRepository.refresh(application.id());
    }
}
