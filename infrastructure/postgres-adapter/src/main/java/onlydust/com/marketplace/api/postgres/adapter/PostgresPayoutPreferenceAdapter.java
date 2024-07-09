package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.PayoutPreferenceStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.PayoutPreferenceEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.PayoutPreferenceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class PostgresPayoutPreferenceAdapter implements PayoutPreferenceStoragePort {

    private final PayoutPreferenceRepository payoutPreferenceRepository;
    private final RewardRepository rewardRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReceivedSomeRewardsOnProject(UserId userId, ProjectId projectId) {
        return rewardRepository.countAllByRecipientIdAndProjectId(userId.value(), projectId.value()) >= 1;
    }

    @Override
    @Transactional
    public void save(ProjectId projectId, BillingProfile.Id billingProfileId, UserId userId) {
        payoutPreferenceRepository.saveAndFlush(PayoutPreferenceEntity.fromDomain(projectId, billingProfileId, userId));
    }
}
