package onlydust.com.marketplace.api.postgres.adapter;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiContributionDataRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiProjectGlobalDataRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiProjectGrantsDataRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiRewardDataRepository;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;

@AllArgsConstructor
public class PostgresBiProjectorAdapter implements AccountingObserverPort, ContributionObserverPort {
    private final BiRewardDataRepository biRewardDataRepository;
    private final BiContributionDataRepository biContributionDataRepository;
    private final BiProjectGrantsDataRepository biProjectGrantsDataRepository;
    private final BiProjectGlobalDataRepository biProjectGlobalDataRepository;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {

    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {

    }

    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        biRewardDataRepository.refresh(rewardId);
    }

    @Override
    public void onRewardCancelled(RewardId rewardId) {
        biRewardDataRepository.refresh(rewardId);
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {
        biRewardDataRepository.refresh(rewardId);
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
        biProjectGlobalDataRepository.refresh(to);
    }

    @Override
    @Transactional
    public void onFundsRefundedByProject(ProjectId from, ProgramId to, PositiveAmount amount, Currency.Id currencyId) {
        biProjectGrantsDataRepository.refresh(to, from);
        biProjectGlobalDataRepository.refresh(from);
    }

    @Override
    @Transactional
    public void onContributionsChanged(Long repoId) {
        biContributionDataRepository.refreshByRepo(repoId);
    }
}
